# CS4472 ATM - Assignment Repository

Course project for CS4472B Software Specification, Testing, and Quality
Assurance (Western University). An ATM system backed by a small bank server,
used as the system under test for the course assignments.

See [TESTING.md](TESTING.md) for the required test/fix workflow, pull requests,
and continuous integration. Your team implements the CI workflow specified
there; writing it is your first commit to your own copy of this repository.

Build: Java 11, Maven (`mvn -B test`).

The demo database: the application reads `bank.db` (SQLite), which is intentionally
not committed to the repository. Recreate it any time from the seed script:

```bash
sqlite3 bank.db < bank-seed.sql
```

(The test suite does not need the database; collaborators are stubbed with Mockito.)

## Development with Docker

The only thing you need installed is Docker Desktop. The container provides
Java 11 (Temurin) and Maven, the same toolchain CI uses. Your checkout is
mounted into it, so edit in your IDE on the host and run inside the container.

```bash
docker compose build                                       # once, or after pom.xml changes
docker compose run --rm atm                                # mvn -B test
docker compose run --rm atm mvn -B checkstyle:check        # lint
docker compose run --rm atm mvn -B test -Dtest=FeesCalculatorTest   # one class
docker compose run --rm atm bash                           # shell, run anything
```

Reports land on the host as usual: `target/surefire-reports/` and
`target/site/jacoco/index.html`. The first run downloads Maven plugins into
the `m2` volume; later runs reuse it. The Swing UI (`Main`) cannot run in the
container; run it on the host if you need it.
