# Metadata schemas

QBiC's metadata model schemas.

## Metadata extract openBIS

The metadata entities in OpenBIS are classified between Samples, Experiments and Data sets. Here is a general overview of the relationship between them. Samples are Highlighted in color, experiments are highlighted in red, and data sets are marked with discontinuous lines. These relationships are modular, and can vary from project to project.

<p align="center">
    <a href="./docs/images/general_scheme.png"><img title="metadata diagram" src="./docs/images/general_scheme.png" width=90%></a>
</p>

For each sample, experiment, or dataset type, there is a set of metadata properties allowed.
The set of sample, experiment, and dataset types with their attached properties defines the metadata schema.
The properties can consist of controlled vocabularies, which are also defined as part of the schema.

### Schema - extracting script

[This groovy](metadata-extract-openbis/openbis-SchemaToJson.groovy) script extracts the current metadata model as stored in OpenBIS. Usage:

```bash
groovy SchemaToJson.groovy credentials.json
```

### Contains latest extracted json schema from OpenBIS

- [sample types](metadata-extract-openbis/openbis-schema/sample_types.json)
- [experiment types](metadata-extract-openbis/openbis-schema/experiment_types.json)
- [dataset types](metadata-extract-openbis/openbis-schema/dataset_types.json)
- [vocabularies](metadata-extract-openbis/openbis-schema/vocabularies.json)

## metadata-sheets-schema

Schema defining the metadata sheets for partner labs and their relationship to the OpenBIS metadata.
