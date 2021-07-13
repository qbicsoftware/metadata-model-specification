"""
Validate all schemas 
"""
import jsonschema
import os, sys
import json

# Find the schemas
files_list = list()
for (dirpath, dirnames, filenames) in os.walk('.'):
    files_list += [os.path.join(dirpath, file) for file in filenames]

# Perform validation
invalid_schemas = list()
for file in files_list:
    if file.endswith(".json"):
        print(f"Validating file: {file}")
        with open(file, "r") as fh:
            json_schema = json.load(fh)

        try:
            jsonschema.Draft7Validator.check_schema(json_schema)
            print(f"Validation successful for file: {file}")
        except jsonschema.exceptions.SchemaError as e:
            print(f"Invalid Schema {file}, {e}")
            invalid_schemas.append(file)
if len(invalid_schemas):
    print("Error: the following schemas were invalid:")
    print(invalid_schemas)
    sys.exit(1)
else:
    print("Validation finished successfully!")