# primrose

A bunch of __experimental__ utility functions for working with collections of futures.

[API Documentation](https://rawgit.com/kouphax/primrose/master/doc/index.html) (Temporary Location)

## Getting it

Primrose is available via [Clojars](https://clojars.org/primrose)

Leiningen

```clojure
[primrose "0.1.0"]
```

Maven

```xml
<dependency>
  <groupId>primrose</groupId>
  <artifactId>primrose</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Usage

Primrose has a very minimal API for working with collections of futures.  The core of the entire library revolves around 2 functions `select-one` and `select-many`.  They both behave in the same way

1. They take a sequence of futures
2. They return a promise that will be __delivered__ when the method logic dictates.

You can import the module diretly

```clojure
(:require [primrose.core :as primrose])
```

The namespace contains a method called `first` which will produce a warning if you import the module via `:refer :all`

```clojure
(:require [primrose.core :refer :all])

; WARNING: first already refers to: #'clojure.core/first in namespace: user, being replaced by: #'primrose.core/first
```

I suggest using only the methods you need or namespacing the imported module (as above) but if this is the way you want to import primrose then you can silence the warning via `:refer-clojure :exclude`

```clojure
(ns myapp.core
  (:require [primrose.core :refer :all])
  (:refer-clojure :exclude [first]))
```

### `select-one`

`select-one` takes a predicate and a set of futures and returns a promise that will eventually hold the value of the first future to return whos result is true for the given predicate.  

To demonstrate this lets imagine we had a method called `get-async` that made a request to a website (off-thread via a future) and eventually returned a response object. Its fictional but you could use [clj-http](https://github.com/dakrone/clj-http) or [http.async.client](http://neotyk.github.io/http.async.client/) to achieve this behavior.

```clojure
(def first-good-result
  (select-one 
    (fn [response] (= 200 (:status response))
    (get-async "http://google.com?q=spoons")
    (get-async "http://yahoo.com?q=spoons")
    (get-async "http://duckduckgo.com?q=spoons")))
 
(println (:host @first-good-result))
=> duckduckgo.com
```

In this example `select-one` will return the first `get-async` response whose status is 200.  If for some reason all 3 fail then `select-one` will return `nil`.

### `first`

A typical use case for `select-one` is to simply return the first future to be `realized` which is simple to implement using the `(fn [_] true)` predicate however this is more boilerplate than you really want.

`first` is a simple wrapper for `select-one` that takes care of sending in this predicate.  The following two forms do the same thing,

```clojure
(select-one
  (fn [_] true)
  (future (Thread/sleep 200) 1)
  (future (Thread/sleep 100) 2))
  
(first
  (future (Thread/sleep 200) 1)
  (future (Thread/sleep 100) 2))
```

### `select-many`

Where `select-one` delivers the promise when the first resulting where the predicate is `true`, `select-many` will wait for all the futures to be `realized` and return a seq of __all__ results that match the predicate.  

For example if we wanted to check a bunch of pages to ensure they exist we could implement this using `select-many`

```clojure
(def not-found-pages
  (fn [response] (= 404 (:status response)))
  (get-async "https://github.com/kouphax/happiness")  
  (get-async "https://github.com/kouphax/sadness")
  (get-async "https://github.com/kouphax/longing")
  (get-async "https://github.com/kouphax/fulfilment"))
  
(println (map :project-name @not-found-pages))
=> [happiness fulfilment]
```

### `all`

A common use case for `select-many` is to fire off all the futures and wait until all the results have been collected and do something with those results.  As with `first` this is easy to implement using the `(fn [_] true)` predicate. 

`all` is a simple wrapper for `select-many` that takes care of sending in this predicate.  The following two forms do the same thing,

```clojure
(select-many
  (fn [_] true)
  (future (Thread/sleep 200) 1)
  (future (Thread/sleep 100) 2))
  
(all
  (future (Thread/sleep 200) 1)
  (future (Thread/sleep 100) 2))
```

## Caveats

- There is currently no internal support for timeouts.  If a future never returns then derefing the promise returned from these methods will block forever.  Timeout can be handled outside of these methods.
- Calling these methods will cause futures to be derefed immediatley even if they were originally part of a lazy-seq.
- There is currently no support for error handling which is likely needed for proper handling of timeouts (even if implemented externally).

## License

Copyright © 2014 James Hughes

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
