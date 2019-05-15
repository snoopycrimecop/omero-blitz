5.5.0-m9 (May 2019)
-------------------

- Bump omero-server version.

5.5.0-m8 (May 2019)
-------------------

- Move repository location properties.
- Fix remaining Javadoc warnings.
- Reduce use of Guava.
- Remove findbugs pulled in by subethamail.

5.5.0-m7 (April 2019)
---------------------

- Remove the Java Gateway code.

5.5.0-m6 (April 2019)
---------------------

- Partially migrate Properties file from the openmicroscopy repository.
- Fix Javadoc warnings.
- Run units test in Travis.

5.5.0-m5 (April 2019)
---------------------

- Deprecate setCaseSentivice in favor of new setCaseSensitive.
- Deprecate vestigial data provider for deleted test.
- Add DEBUG logging for successful repository file deletions.
- Safely order repository file deletions.
- Move OMEROImportFixture into test directory.
- LegalGraphTargets can work for any GraphQuery instance.
- Java SSL fixes for clients (rebased from metadata54).
- Remove Repository.pixels and deprecate BfPixelsStoreI.
- Fix "cannot link" bug with import target containers.
- Adjust some Blitz API responses to signal "OK".
- Remove from comments confusing references to OMERO 4.x code.
- Update Hibernate DTD URL to current recommendation.
- Version loading tweak.
- Fix comments in FS Blitz API.
- Include map annotations in image metadata export.
- Add License file.

5.5.0-m4 (March 2019)
---------------------

- Use new Gradle build system.

5.5.0-m3 (February 2019)
------------------------

- Extract omero-common from the openmicroscopy repository.
