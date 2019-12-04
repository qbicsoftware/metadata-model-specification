@GrabResolver(name='maven-releases', root='https://qbic-repo.am10.uni-tuebingen.de/repository/maven-releases')
@Grab(group='life.qbic.openbis', module='openbis-api', version='18.06.2', classifier='r1541498074')
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi
import ch.systemsx.cisd.common.spring.HttpInvokerUtils
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions
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
    IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, url, 10000)
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

def criteria = new VocabularySearchCriteria()
def fetchOptions = new VocabularyFetchOptions()
fetchOptions.withTerms()

def searchResult = apiConnection.searchVocabularies(token, criteria, fetchOptions)

def finalJsonContent = [:]

searchResult.getObjects().each {
    def propertyContent = [:]
    propertyContent["type"] = "string"
    propertyContent["description"] = it.description
    //propertyContent["id"] = it.code.toLowerCase()
    propertyContent["enum"] = it.terms.collect { it.code }
    finalJsonContent[it.code] = propertyContent
}

def vocabularyFile = new File("schema/vocabularies.json")
vocabularyFile.withWriter {
    it.write JsonOutput.prettyPrint(JsonOutput.toJson(["definitions": finalJsonContent]))
}

//Finally log out from openBIS
apiConnection.logout(token)


