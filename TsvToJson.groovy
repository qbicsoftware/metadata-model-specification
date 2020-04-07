@GrabResolver(name='maven-releases', root='https://qbic-repo.am10.uni-tuebingen.de/repository/maven-releases')
@Grab(group='life.qbic.openbis', module='openbis-api', version='18.06.2', classifier='r1541498074')
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi
import ch.systemsx.cisd.common.spring.HttpInvokerUtils
// Vocabulary
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions
// Sample
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

final int SERVER_TIMEOUT = 30000

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
    IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, url, SERVER_TIMEOUT)
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

//----------------------
// Define sample search
//----------------------
def sampleCriteria = new SampleSearchCriteria()
def sampleFetchOptions = new SampleFetchOptions()
sampleFetchOptions.withType()

def sampleSearchResult = apiConnection.searchSamples(token, sampleCriteria, sampleFetchOptions)

def sampleJsonContent = [:]

sampleSearchResult.getObjects().each {
    def propertyContent = [:]
    propertyContent["description"] = it.description
    //propertyContent["id"] = it.code.toLowerCase()
    propertyContent["enum"] = it.terms.collect { it.code }
    finalJsonContent[it.code] = propertyContent
}

def sampleFile = new File("schema/samples.json")
sampleFile.withWriter {
    it.write JsonOutput.prettyPrint(JsonOutput.toJson(["definitions": sampleJsonContent]))
}



//Finally log out from openBIS
apiConnection.logout(token)


