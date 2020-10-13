# psfclj

FIXME: description

## Installation

Download from http://example.com/FIXME.

## Usage

FIXME: explanation

    $ java -jar psfclj-0.1.0-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.

## Examples

...

## PSF BNF

```
<psf> -> <section>
<section> -> "HEADER" <contents> <section>
           | "TYPE" <contents> <section>
           | "VALUE" <contents> <section>
           | "SWEEP" <contents> <section>
           | "TRACE" <contents> <section>
           | "END"
<contents> -> <content> <contents>
            | <content> <struct>
            | <content> <struct> <contents>
            | <content>
<content> -> #"\"\S+\"" <values>
           | #"\"\S+\"" <unit> <values>
           | #"\"\S+\"" <type> <type> <prop>
<type> -> "FLOAT"
        | "DOUBLE"
        | "COMPLEX"
<prop> -> "PROP(" <contents> ")"
<struct> -> "STRUCT(" <contents> ")"
          | "STRUCT(" <contents> ")" <prop>
<unit> -> #"\"\S+\""
<values> -> <value>
          | <value> <values>
          | "(" <values> ")"
<value> -> #"\"\S+\""
```

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2020 Yannick Uhlmann

This program and the accompanying materials are made available under the
terms of the the beer-ware license (Revision 42):
As long as you retain this notice you can do whatever you want with this stuff. 
If we meet some day, and you think this stuff is worth it, 
you can buy me a beer in return.
