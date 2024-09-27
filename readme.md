



## Design decisions

* Queries implementations - due to fact that quill queries return effects that can fail with SQLError all effects should die with DatabaseCriticalFailure