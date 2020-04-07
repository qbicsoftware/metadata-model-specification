@GrabResolver(name='maven-releases', root='https://qbic-repo.am10.uni-tuebingen.de/repository/maven-releases')
@Grab(group='life.qbic.openbis', module='openbis-api', version='18.06.2', classifier='r1541498074')
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi
import ch.systemsx.cisd.common.spring.HttpInvokerUtils
// Vocabulary
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions
// Sample
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def parseCredentialsFromJsonFile(String path) {
    try {
        def content = new JsonSlurper().parseText(new File(path).text)
    } catch (Exception e) {
        println "Could not parse credential JSON file, please check file format!"
    }
}

def loginToOpenBis(String user, String pw) {
    // get a reference to AS API
    def url = "https://qbis.qbic.uni-tuebingen.de/openbis/openbis" + IApplicationServerApi.SERVICE_URL
    IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, url, 100000)
    // login to obtain a session token
    sessionToken = v3.login(user, pw)
    return [sessionToken, v3]
}
// Parse openBIS credentials from JSON file
def credentialsJsonPath = args[0]
def credentials = parseCredentialsFromJsonFile(credentialsJsonPath)
def sessionToken = ""

// Open connection to openBIS server
(token, apiConnection)  = loginToOpenBis(credentials.user, credentials.pw)

// ------------------------
// Define vocabulary search
// -------------------------

def vocabularyCriteria = new VocabularySearchCriteria()
def vocabularyFetchOptions = new VocabularyFetchOptions()
vocabularyFetchOptions.withTerms()

def vocabularySearchResult = apiConnection.searchVocabularies(token, vocabularyCriteria, vocabularyFetchOptions)

def vocabularyJsonContent = [:]

vocabularySearchResult.getObjects().each {
    System.out.println("Vocabulary " + it.code + it.description + it.terms.collect{ it.code } )
    def propertyContent = [:]
    propertyContent["type"] = "string"
    propertyContent["description"] = it.description
    //propertyContent["id"] = it.code.toLowerCase()
    propertyContent["enum"] = it.terms.collect { it.code }
    vocabularyJsonContent[it.code] = propertyContent
}

def vocabularyFile = new File("schema/vocabularies.json")
vocabularyFile.withWriter {
    it.write JsonOutput.prettyPrint(JsonOutput.toJson(["definitions": vocabularyJsonContent]))
}

//---------------------------
// Define sample type search
//---------------------------
def sampleTypeSearchCriteria = new SampleTypeSearchCriteria()
def sampleTypeFetchOptions = new SampleTypeFetchOptions()
sampleTypeFetchOptions.withPropertyAssignments().withPropertyType()

def sampleTypeSearchResult = apiConnection.searchSampleTypes(token, sampleTypeSearchCriteria, sampleTypeFetchOptions)

def sampleTypeJsonContent = [:]

sampleTypeSearchResult.getObjects().each {
    System.out.println("Sample " + it.code + " " + it.getPropertyAssignments() )
    def sampleContent = [:]
    it.getPropertyAssignments().each {
        System.out.println("Assignment " + it.getPropertyType().code + " " + it.getPropertyType().getDescription() )
        def propertyContent = [:]
        propertyContent["type"] = "string"
        propertyContent["description"] = it.getPropertyType().getDescription()
        sampleContent[it.getPropertyType().code] = propertyContent
    }
    sampleTypeJsonContent[it.code] = sampleContent
}

def sampleFile = new File("schema/sample_types.json")
sampleFile.withWriter {
    it.write JsonOutput.prettyPrint(JsonOutput.toJson(["definitions": sampleTypeJsonContent]))
}



//Finally log out from openBIS
apiConnection.logout(token)


