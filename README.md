# ![](./img/converter.svg) Unit Converter

## Measure File Format

To add custom measures you can use editor build in app or you can prepare the json file:

``` json
{
  "name": {
    "en": "Area",
    "pl": "Powierzchnia"
  },
  "version": 1,
  "author": [
    "root"
  ],
  "global": "en",
  "units": [
    {
      "symbol": "a",
      "description": {
        "en": "are",
        "pl": "ar"
      },
      "position": -1,
      "one": 100,
      "prefixes": [
        {
          "symbol": "h",
          "description": {
            "en": "hect",
            "pl": "hekt"
          },
          "exp": 2
        }
      ]
    },
    {
      "symbol": "m²",
      "descriptionPrefix": {
        "en": "square ",
        "pl": ""
      },
      "description": {
        "en": "meter",
        "pl": "metr kwadratowy"
      },
      "one": 1,
      "prefixes": [
        {
          "symbol": "k",
          "description": {
            "en": "kilo"
          },
          "exp": 6,
          "position": 3
        },
        {
          "symbol": "d",
          "description": {
            "en": "deci",
            "pl": "decy"
          },
          "exp": -2
        },
        {
          "symbol": "c",
          "description": {
            "en": "centi",
            "pl": "centy"
          },
          "": -4
        },
      ]
    },
    {
      "symbol": "in²",
      "description": {
        "en": "square inch",
        "pl": "cal kwadratowy"
      },
      "one": 0.00064516
    }
  ]
}

```
You can find more measures files on this folder: [./app/src/main/assets/converters](./app/src/main/assets/converters)

### Properties
|property|value|
|---|-----|
|file|`{name, version, author, global, units}`|
|||
|`name`|`{"country_code" : "`measure name`"}`|
|`version`|integer|
|`author`|`["`author name`"]`|
|`global`|base country code name|
|`units`|`[{symbol, descriptionPrefix, description, one, shift, shift2, prefixes}]`|
|||
|`symbol`|base unit symbol|
|`descriptionPrefix`|`{"country_code" : "`description prefix`"}`|
|`description`|`{"country_code" : "`description`"}`|
|`one`|number of multiples of base unit|
|`shift`|number of shift to the basic unit|
|`shift2`|number of shift to the basic unit|
|`prefixes`|`[{symbol, description, exp}]`|
|||
|`symbol`|prefix symbol|
|`description`|`{"country_code" : "`prefix description`"}`|
|`exp`|unit exponent|

### Equation

```math
y = \alpha*(x + \Delta_1) + \Delta_2
```
where is:
|symbol|description|default|
|-|-|-|
|x|source unit value||
|y|target unit value||
|$\alpha$|`one` - multiply of base value|`1`|
|$\Delta_1$|`shift_1` - shift base value before multiply|`0`|
|$\Delta_2$|`shift_2` - shift base value after multiply|`0`|

## Screens

![](./img/en/converter-blue.png)
![](./img/en/converter-units.png)
![](./img/en/converter-measures.png)
![](./img/en/converter-landscape.png)
![](./img/en/converter-green.png)
![](./img/en/converter-red.png)

## License
MIT
