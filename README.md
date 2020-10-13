# psfclj

PSF parser for JVM written in clojure.

## Installation

Download from http://example.com/FIXME.

## Usage

```bash
    $ java -jar psfclj-0.1.0-standalone.jar [args]
```

## Options

```bash
psfclj [options] <file>
```
Where `[options]` are:

- `-g <grammar>` Specify path to alternative `<grammar>`.
- `-j` JSON output (default).
- `-c` CSV output (values only).

and `<file>` is a PSF.

## Examples

...

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

### Bugs

- [ ] Can't read PSF with `<unit>` in `VALUE` section.

## License

Copyright © 2020 Yannick Uhlmann

This program and the accompanying materials are made available under the
terms of the the beer-ware license (Revision 42):
As long as you retain this notice you can do whatever you want with this stuff. 
If we meet some day, and you think this stuff is worth it, 
you can buy me a beer in return.
