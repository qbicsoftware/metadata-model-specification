# metadata-model

QBiC's OpenBIS metadata model schema.

## General overview

The metadata entitites in OpenBIS are classified between Samples, Experiments and Data sets. Here is a general overview of the relationship between them, but it is modular and it can vary from project to project.

<p align="center">
    <a href="./docs/images/general_scheme.png"><img title="metadata diagram" src="./docs/images/general_scheme.png" width=90%></a>
</p>

## Schema

Contains json schema extracted from OpenBIS for:

- [vocabularies](schema/vocabularies.json)
- [sample types](schema/sample_types.json)
- [experiment types](schema/experiment_types.json)
- [dataset types](schema/dataset_types.json)

## metadata-sheets-schema

Schema defining the metadata sheets for partner labs and their relationship to the OpenBIS metadata.

### Schema - extracting script

[This](SchemaToJson.groovy) script extracts the current metadata model as stored in OpenBIS. Usage:

```bash
groovy SchemaToJson.groovy credentials.json
```
