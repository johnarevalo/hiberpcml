You can set additional attributes in your pcml files in order to make easier customization names and behavior of POJO Classes.

### `<data>` ###

  * **label**: Setting this attribute the default name for this data is overriden in Java field
  * **completeWith**: padded the value to the left with given string

### `<program>` ###

  * **label**: Setting this attribute the default name for Java class is overriden

### `<struct>` ###

  * **label**: Setting this attribute the default name for Java class which represents this structure is overriden

### `<pcml>` ###

  * **webMethodName**: (_Mandatory attribute if -w option is given_). Defines name of the published method in the web service for this program.

#### See Also ####
UsingGenerator