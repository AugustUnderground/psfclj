# psfclj

PSF parser for JVM written in clojure.

## Building

Requires [Leiningen](https://leiningen.org/index.html) to run, see
their docs for more information.

### Build and Install as MAVEN dependency for Java InterOp

```bash
$ lein pom && lein install
```

### Build JAR/UBERJAR

```bash
$ lein jar
$ lein uberjar
```

### Build Standalone Executable

Simply run `make` and the `Makefile` will take care of it.
The executable will be `./target/psfconvert`.

## Usage

After building with lein you can run:

```bash
$ java -jar psfclj-0.1.0-standalone.jar [options] <psf-file>
```

The standalone executable can be run like so:

```bash
$ psfconvert [options] <psf-file>
```

Depending on the option, this will produce output to `STDOUT`.
This can be redirected to a file, or processed further with something
like [jq](https://stedolan.github.io/jq/) 
or [gron](https://github.com/TomNomNom/gron).
If no `<psf-file>` is specified, it will try to read from `STDIN`.

### Options

- `-g <grammar>` Specify path to alternative `<grammar>`.
- `-j` JSON output (default).
- `-c` CSV output (values only).
- `-h` or `--help` for a short help.

### Exit States

- `0`: Success!
- `-1`: Failed with parse error, usually caused by wrong sytnax.
- `-2`: Erronious command line arguments.
- `-3`: No output format specified.

### Example

```bash
# Read ./noise2.noise, pipe json output in gron, 
# grep for "fn.xf2" of "I0.M0.m1" and inspect with bat
$ ./target/psfconvert -j ./noise2.noise | gron | grep "I0.M0.m1" | grep "fn.xf2" | bat

# Pipe json ouput into fzf for interactive search
$ ./target/psfconvert -j ./noise2.noise | gron | fzf

# Read from stdin and redirect output into file
$ cat ./noise2.noise | ./target/psfconvert -c > noise2.csv
```

## PSF BNF

The default grammar can be found in `resources/psf.bnf`:

```bnf
<psf> = <section> *

<section> = HEADER <attribute> *
          | TYPE   <attribute> *
          | SWEEP  <attribute> *
          | TRACE  <attribute> *
          | VALUE  <attribute> *
          | END

<attribute> = <key> [<unit>] <values> [<prop>]
            | <key> <types> <prop>
            | <key> <struct> [<prop>]

<key> = " * "

<unit> = " * "

values = <value>
       | ( <value> * )

<value> = #'\S+'
        | " * "

<types> = <type>
        | <type> *

<type> = FLOAT
       | DOUBLE
       | COMPLEX

<prop> = PROP( <attribute> * )

<struct> = STRUCT( <attribute> * )
```

## Java API Reference

After installing with 

```bash
$ lein pom && lein install
``` 

the `psfclj*.jar` should be in your local repository and classpath.

### Example

```java
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import clojure.lang.APersistentMap;
import psfclj.PSFParser.Parser;

public class PSFParserExample {

  public static void main(String[] args) throws IOException {
    
    Parser parser = new Parser();
    
    String psfContent = FileUtils.readFileToString(
        new File("./noise2.noise"));
    
    APersistentMap map = parser.parsePSF(psfContent);
      
  }
}
```

## TODO

- [X] CSV Output
- [ ] Comment Code
- [ ] Read PSF with `<unit>` in `VALUE` section.
- [ ] Implemented tests
- [ ] XML Output

## License

Copyright © 2020 Yannick Uhlmann

This program and the accompanying materials are made available under the
terms of the the beer-ware license (Revision 42):
As long as you retain this notice you can do whatever you want with this stuff. 
If we meet some day, and you think this stuff is worth it, 
you can buy me a beer in return.
