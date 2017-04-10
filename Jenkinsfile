#!groovy

node {
  try {
    def mvnHome
    stage('\u27A1 Preparation') {
      git 'git@github.com:openwms/org.openwms.tms.transportation.git'
      mvnHome = tool 'M3'
    }
    stage('\u27A1 Build') {
      configFileProvider(
          [configFile(fileId: 'maven-local-settings', variable: 'MAVEN_SETTINGS')]) {
            sh "'${mvnHome}/bin/mvn' -s $MAVEN_SETTINGS clean install -Dci.buildNumber=${BUILD_NUMBER} -Ddocumentation.dir=${WORKSPACE}/target -Psordocs,sonatype -U"
      }
    }
    stage('\u27A1 Heroku Staging') {
      sh '''
        if git remote | grep heroku > /dev/null; then
           git remote remove heroku
        fi
        git remote add heroku https://:${HEROKU_API_KEY}@git.heroku.com/openwms-tms-transportation.git
        git push heroku master -f
      '''
    }
    stage('\u27A1 Results') {
      archive '**/target/*.jar'
    }
    stage('\u27A1 Documentation') {
      configFileProvider(
          [configFile(fileId: 'maven-local-settings', variable: 'MAVEN_SETTINGS')]) {
            sh "'${mvnHome}/bin/mvn' -s $MAVEN_SETTINGS site site:deploy -Dci.buildNumber=${BUILD_NUMBER} -Psonatype"
      }
    }
    stage('\u27A1 Sonar') {
      sh "'${mvnHome}/bin/mvn' clean org.jacoco:jacoco-maven-plugin:prepare-agent sonar:sonar -Djacoco.propertyName=jacocoArgLine -Pjenkins"
    }
  } finally {
    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
  }
}

