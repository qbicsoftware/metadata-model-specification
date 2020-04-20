# metadata-model

QBiC's OpenBIS metadata model schema.

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
