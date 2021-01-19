// Usage:
// groovy SchemaToJson.groovy credentials.json

@GrabResolver(name='maven-releases', root='https://qbic-repo.qbic.uni-tuebingen.de/repository/maven-releases')
@Grab(group='life.qbic', module='openbis-api', version='18.06.2')
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi
import ch.systemsx.cisd.common.spring.HttpInvokerUtils
// Vocabulary classes
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions
// Sample type classes
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions
// Experiment type classes
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions
// Dataset type classes
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions
// Json imports
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

//-------------------
// Login to openbis
//-------------------

def parseCredentialsFromJsonFile(String path) {
    try {
        def content = new JsonSlurper().parseText(new File(path).text)
    } catch (Exception e) {
        println 'Could not parse credential JSON file, please check file format!'
    }
}

def loginToOpenBis(String user, String pw) {
    // get a reference to AS API
    def url = 'https://openbis1605test.am10.uni-tuebingen.de/openbis/openbis' + IApplicationServerApi.SERVICE_URL
    // Increase the server timeout (10000) if you get a server timeout excheption for exhaustive queries
    IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, url, 10000)
    // login to obtain a session token
    sessionToken = v3.login(user, pw)
    return [sessionToken, v3]
}

def convertSchemaType(String openBISType) {
    // Convert openBIS data type to correct JSON Schema equivalent
    String newType = 'string'
    switch (openBISType) {
        case 'VARCHAR':
            newType = 'string'
            break
        case 'MULTILINE_VARCHAR':
            newType = 'string'
            break
        case 'XML':
            newType = 'string'
            break
        case 'REAL':
            newType = 'number'
            break
        case 'CONTROLLEDVOCABULARY':
            newType = 'string'
            break
        case 'TIMESTAMP':
            newType = 'string'
            break
        case 'BOOLEAN':
            newType = 'boolean'
        case 'INTEGER':
            newType = 'integer'
            break
        default:
            println("Unknown openBIS type encountered! ${openBISType}")
    }
    return newType
}

def replaceNull(String x) {
    // Replace null with ""
    if (x) {
        return x
    }
    else {
        return ''
    }
}

// Parse openBIS credentials from JSON file
def credentialsJsonPath = args[0]
def credentials = parseCredentialsFromJsonFile(credentialsJsonPath)
def sessionToken = ''

// Open connection to openBIS server
(token, apiConnection)  = loginToOpenBis(credentials.user, credentials.pw)

// -----------------
//  Vocabulary
// -----------------
def vocabularyCriteria = new VocabularySearchCriteria()
def vocabularyFetchOptions = new VocabularyFetchOptions()
vocabularyFetchOptions.withTerms()

def vocabularySearchResult = apiConnection.searchVocabularies(token, vocabularyCriteria, vocabularyFetchOptions)

def vocabularyJsonContent = [:]

vocabularySearchResult.getObjects().each {
    //System.out.println("Vocabulary " + it.code + it.description + it.terms.collect{ it.code } )
    def propertyContent = [:]
    propertyContent['type'] = 'string'
    propertyContent['description'] = replaceNull( it.description )
    //propertyContent["id"] = it.code.toLowerCase()
    propertyContent['enum'] = it.terms.collect { it.code }
    vocabularyJsonContent[it.code] = propertyContent
}

def vocabularyFile = new File('schema-test/vocabularies.json')
vocabularyFile.withWriter {
    it.write JsonOutput.prettyPrint(JsonOutput.toJson(['definitions': vocabularyJsonContent]))
}

//---------------------------
// Sample type
//---------------------------
def sampleTypeSearchCriteria = new SampleTypeSearchCriteria()
def sampleTypeFetchOptions = new SampleTypeFetchOptions()
sampleTypeFetchOptions.withPropertyAssignments().withPropertyType()

def sampleTypeSearchResult = apiConnection.searchSampleTypes(token, sampleTypeSearchCriteria, sampleTypeFetchOptions)

def sampleTypeJsonContent = [:]

sampleTypeSearchResult.getObjects().each {
    //System.out.println("Sample " + it.code + " " + it.getPropertyAssignments() )
    def sampleContent = [:]

    def samplePropertiesContent = [:]
    it.getPropertyAssignments().each {
        //System.out.println("Assignment " + it.getPropertyType().code + " " + it.getPropertyType().getDescription() )
        def propertyContent = [:]
        propertyContent['type'] = convertSchemaType( it.getPropertyType().getDataType().toString() )
        propertyContent['@comment'] = it.getPropertyType().getDataType()
        propertyContent['label'] = it.getPropertyType().getLabel()
        propertyContent['description'] = it.getPropertyType().getDescription()
        samplePropertiesContent[it.getPropertyType().code] = propertyContent
    }
    sampleContent['description'] = replaceNull( it.description )
    sampleContent['properties'] = samplePropertiesContent
    sampleTypeJsonContent[it.code] = sampleContent
}

def sampleFile = new File('schema-test/sample_types.json')
sampleFile.withWriter {
    it.write JsonOutput.prettyPrint(JsonOutput.toJson(['definitions': sampleTypeJsonContent]))
}

//------------------------
// Experiment type
//------------------------
def experimentTypeSearchCriteria = new ExperimentTypeSearchCriteria()
def experimentTypeFetchOptions = new ExperimentTypeFetchOptions()
experimentTypeFetchOptions.withPropertyAssignments().withPropertyType()

def experimentTypeSearchResult = apiConnection.searchExperimentTypes(token, experimentTypeSearchCriteria, experimentTypeFetchOptions)

def experimentTypeJsonContent = [:]

experimentTypeSearchResult.getObjects().each {
    System.out.println('Experiment ' + it.code + ' ' + it.getPropertyAssignments())
    def experimentContent = [:]

    def experimentPropertiesContent = [:]
    it.getPropertyAssignments().each {
        System.out.println('Assignment ' + it.getPropertyType().code + ' ' + it.getPropertyType().getDescription() )
        def propertyContent = [:]
        propertyContent['type'] = convertSchemaType( it.getPropertyType().getDataType().toString() )
        propertyContent['@comment'] = it.getPropertyType().getDataType()
        propertyContent['label'] = it.getPropertyType().getLabel()
        propertyContent['description'] = it.getPropertyType().getDescription()
        experimentPropertiesContent[it.getPropertyType().code] = propertyContent
    }
    experimentContent['description'] = replaceNull( it.description )
    experimentContent['properties'] = experimentPropertiesContent
    experimentTypeJsonContent[it.code] = experimentContent
}

def experimentFile = new File('schema-test/experiment_types.json')
experimentFile.withWriter {
    it.write JsonOutput.prettyPrint(JsonOutput.toJson(['definitions': experimentTypeJsonContent]))
}

//--------------------------
// Data set type
//--------------------------

def datasetTypeSearchCriteria = new DataSetTypeSearchCriteria()
def datasetTypeFetchOptions = new DataSetTypeFetchOptions()
datasetTypeFetchOptions.withPropertyAssignments().withPropertyType()

def datasetTypeSearchResult = apiConnection.searchDataSetTypes(token, datasetTypeSearchCriteria, datasetTypeFetchOptions)

def datasetTypeJsonContent = [:]

datasetTypeSearchResult.getObjects().each {
    System.out.println('Dataset ' + it.code + ' ' + it.getPropertyAssignments())
    def datasetContent = [:]

    def datasetPropertiesContent = [:]
    it.getPropertyAssignments().each {
        System.out.println('Assignment ' + it.getPropertyType().code + ' ' + it.getPropertyType().getDescription() )
        def propertyContent = [:]
        propertyContent['type'] = convertSchemaType( it.getPropertyType().getDataType().toString() )
        propertyContent['@comment'] = it.getPropertyType().getDataType()
        propertyContent['label'] = it.getPropertyType().getLabel()
        propertyContent['description'] = it.getPropertyType().getDescription()
        datasetPropertiesContent[it.getPropertyType().code] = propertyContent
    }
    datasetContent['description'] = replaceNull( it.description )
    datasetContent['properties'] = datasetPropertiesContent
    datasetTypeJsonContent[it.code] = datasetContent
}

def datasetFile = new File('schema-test/dataset_types.json')
datasetFile.withWriter {
    it.write JsonOutput.prettyPrint(JsonOutput.toJson(['definitions': datasetTypeJsonContent]))
}

//Finally log out from openBIS
apiConnection.logout(token)
