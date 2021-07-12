"""
Validate all schemas 
"""
import jsonschema
import os
import json

# Find the schemas
schema_path = os.path.expanduser(".")

# Perform validation
for file in os.listdir(schema_path):
    if file.endswith(".json"):
        print("Validating file: {file}")
        with open(file, "r") as fh:
            json_schema = json.load(fh)

        try:
            jsonschema.Draft7Validator.check_schema(json_schema)
        except jsonschema.exceptions.SchemaError as e:
            print(f"Invalid Schema {file}, {e}")

print("Validation successful!")