# Quality Clojure

Use a number of existing tools like `kibit`, `eastwood`, and `bikeshed` to get feedback about a particular library. When it's finished, you'll be able to point to a github repository, and the service will suck it in. From that point, there are a few things that will happen:

* import the repository into datomic
* run kibit, import comments into datomic
* run eastwood, import comments into datomic
* run bikeshed, import comments into datomic
* run pygments on each source file to output a static html file for display 

At this point, you can access the repository at `/repo/<github user or org>/<repo-name>/` and get a listing of the files available to view, as well as overall stats for the repository. From here, you can drill down and view individual files and the notes and comments associated with each one.

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

You will also need an installation of [Datomic Free][2]

[1]: https://github.com/technomancy/leiningen
[2]: https://my.datomic.com/downloads/free

## Running

First, start the Datomic transactor:

    bin/transactor config/samples/free-transactor-template.properties

Then, start figwheel, which will send updated compiled javascript to the browser:

    lein figwheel

To start a web server for the application:

    lein ring server
    
## TODO
Check out the [issues!](https://github.com/jcsims/qualityclj/issues)

## License

Copyright © 2014 Chris Sims and Scott Bauer
