/*
 * Copyright (c) 2025 Paul (Thor) Thordarson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dbmcp.database

import java.nio.file.{ Files, Paths }
import java.time.LocalDateTime

import scala.io.Source
import scala.util.Using

import cats.effect.{ IO, Resource }
import cats.implicits._
import doobie._
import doobie.implicits._

case class Migration(version: Int, scriptName: String, sqlContent: String)

object Database {

  private def resolveDbPath(): String = {
    Option(System.getenv("DECKBUILDER_DB_PATH"))
      .getOrElse(System.getProperty("user.home") + "/.deckbuilder-mcp/cards.db")
  }

  private val dbPath = resolveDbPath()

  val transactor: Resource[IO, Transactor[IO]] = {
    Resource.eval(ensureDbDirectoryExists(dbPath)) *>
      Resource.pure(
        Transactor.fromDriverManager[IO](
          driver = "org.sqlite.JDBC",
          url = s"jdbc:sqlite:$dbPath",
          logHandler = None
        )
      )
  }

  private def ensureDbDirectoryExists(dbPath: String): IO[Unit] = {
    IO {
      val dbDir = Paths.get(dbPath).getParent
      val userHome = Paths.get(System.getProperty("user.home"))

      // Check if this is a hidden directory in user home
      val isHiddenInUserHome = dbDir.startsWith(userHome) &&
        dbDir.getFileName.toString.startsWith(".")

      (dbDir, isHiddenInUserHome, Files.exists(dbDir))
    }.flatMap { case (dbDir, isHiddenInUserHome, dirExists) =>
      if (isHiddenInUserHome) {
        // Hidden directory in user home - create if needed
        if (!dirExists) {
          IO(Files.createDirectories(dbDir)).void
        } else {
          IO.unit
        }
      } else {
        // Non-hidden or non-home directory - must exist
        if (!dirExists) {
          IO.raiseError(
            new RuntimeException(
              s"Database directory does not exist: $dbDir. " +
                "Please create the directory or use a hidden directory in your home folder."
            )
          )
        } else {
          IO.unit
        }
      }
    }
  }

  private def databaseExists(): Boolean = {
    Files.exists(Paths.get(dbPath))
  }

  def initializeOrMigrate(): IO[Unit] = {
    transactor.use { xa =>
      if (databaseExists()) {
        upgradeIfNeeded(xa)
      } else {
        createFreshDatabase(xa)
      }
    }
  }

  private def createFreshDatabase(xa: Transactor[IO]): IO[Unit] = {
    for {
      migrations <- loadMigrations()
      _ <- runAllMigrations(migrations, xa)
      _ <- IO.println(
        s"Created new database with ${migrations.length} migrations applied"
      )
    } yield ()
  }

  private def upgradeIfNeeded(xa: Transactor[IO]): IO[Unit] = {
    for {
      currentVersion <- getCurrentVersion(xa)
      migrations <- loadMigrations()
      targetVersion = migrations.map(_.version).maxOption.getOrElse(0)
      _ <-
        if (currentVersion < targetVersion) {
          for {
            backupPath <- createBackup()
            _ <- runMigrations(
              migrations.filter(_.version > currentVersion),
              xa
            )
              .handleErrorWith(error => restoreBackupAndFail(backupPath, error))
            _ <- IO.println(
              s"Database upgraded from version $currentVersion to $targetVersion"
            )
          } yield ()
        } else {
          IO.println(s"Database is up to date (version $currentVersion)")
        }
    } yield ()
  }

  private def getCurrentVersion(xa: Transactor[IO]): IO[Int] = {
    sql"SELECT MAX(version) FROM schema_version"
      .query[Option[Int]]
      .unique
      .transact(xa)
      .map(_.getOrElse(0))
      .handleErrorWith(_ => IO.pure(0)) // Table doesn't exist yet
  }

  private def loadMigrations(): IO[List[Migration]] = {
    IO {
      val resourcePath = "/migrations"
      val migrationFiles = List("001_schema_version.sql", "002_owned_cards.sql")

      migrationFiles
        .map { fileName =>
          val version = fileName.take(3).toInt
          val content = Using.resource(
            getClass.getResourceAsStream(s"$resourcePath/$fileName")
          ) { stream =>
            Source.fromInputStream(stream).mkString
          }
          Migration(version, fileName, content)
        }
        .sortBy(_.version)
    }
  }

  private def runAllMigrations(
      migrations: List[Migration],
      xa: Transactor[IO]
  ): IO[Unit] = {
    migrations.traverse_ { migration =>
      runSingleMigration(migration, xa)
    }
  }

  private def runMigrations(
      migrations: List[Migration],
      xa: Transactor[IO]
  ): IO[Unit] = {
    migrations.traverse_ { migration =>
      runSingleMigration(migration, xa)
    }
  }

  private def runSingleMigration(
      migration: Migration,
      xa: Transactor[IO]
  ): IO[Unit] = {
    IO.println(s"Applying migration ${migration.scriptName}") *>
      Fragment.const(migration.sqlContent).update.run.transact(xa).void
  }

  private def createBackup(): IO[String] = {
    IO {
      val timestamp = LocalDateTime.now().toString.replace(":", "-")
      val backupPath = s"${dbPath}.backup-$timestamp"
      val process = Runtime.getRuntime.exec(
        Array("sqlite3", dbPath, s".backup '$backupPath'")
      )
      val exitCode = process.waitFor()
      (exitCode, backupPath)
    }.flatMap { case (exitCode, backupPath) =>
      if (exitCode == 0) {
        IO.pure(backupPath)
      } else {
        IO.raiseError(new RuntimeException("Failed to create database backup"))
      }
    }
  }

  private def restoreBackupAndFail(
      backupPath: String,
      error: Throwable
  ): IO[Unit] = {
    IO {
      val process = Runtime.getRuntime.exec(Array("cp", backupPath, dbPath))
      process.waitFor()
    } *> IO.raiseError(
      new RuntimeException(
        s"Database migration failed and backup restored. Original error: ${error.getMessage}. " +
          s"Please ensure application version matches database version or restore from $backupPath manually."
      )
    )
  }

  // Legacy method for backward compatibility - now just calls initializeOrMigrate
  val initializeDb: ConnectionIO[Unit] = {
    // This is now handled by initializeOrMigrate()
    Fragment.const("SELECT 1").query[Int].unique.void
  }
}
