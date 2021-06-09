"""
Validate all schemas 
"""
import jsonschema
import os
import json

# Find the schemas
schema_path = "metadata-test-openbis/schema-test/"
schemas = os.listdir(schema_path)

# Perform validation
for schema in schemas:
    with open(os.path.join(schema_path, schema), "r") as fh:
        json_schema = json.load(fh)

    try:
        jsonschema.Draft7Validator.check_schema(json_schema)
    except jsonschema.exceptions.SchemaError as e:
        print(f"Invalid Schema {schema}, {e}")

print("Validation successfull!")