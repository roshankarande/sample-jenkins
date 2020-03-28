stage 'CI'
node {

   checkout scm

    // pull dependencies from npm
    bat 'npm install'
    //sh 'npm install'

    // stash code & dependencies to expedite subsequent testing
    // and ensure same code & dependencies are used throughout the pipeline
    // stash is a temporary archive
    stash name: 'everything', 
          excludes: 'test-results/**', 
          includes: '**'
    
    // test with PhantomJS for "fast" "generic" results
    // bat 'npm run test-single-run -- --browsers PhantomJS'
    // sh 'npm run test-single-run -- --browsers PhantomJS'
    
    // archive karma test results (karma is configured to export junit xml files)
    //step([$class: 'JUnitResultArchiver', 
    //      testResults: 'test-results/**/test-results.xml'])
          
}


node('windows') {
    bat 'dir'
    bat 'del /S /Q *'
    unstash 'everything'
    bat 'dir'
    
    //bat 'npm run test-single-run -- --browsers Chrome'

}

stage 'Browser Testing'
parallel chrome: {
     runTests("Chrome")
 }, firefox: {
     runTests("Firefox")
 }, phantomJS: {
     runTests("PhantomJS")
 }


def runTests(browser) {
    node {
         bat 'del /S /Q *'
        //sh 'rm -rf *'

        unstash 'everything'

        bat "npm run test-single-run -- --browsers ${browser}"
        //sh "npm run test-single-run -- --browsers ${browser}"

        step([$class: 'JUnitResultArchiver', 
              testResults: 'test-results/**/test-results.xml'])
    }
}


def notify(status){
    emailext (
      to: "wesmdemos@gmail.com",
      subject: "${status}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
      body: """<p>${status}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
        <p>Check console output at <a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>""",
    )
}