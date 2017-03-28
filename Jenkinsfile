#!/usr/bin/env groovy

node {
  try {
    def mvnHome
    stage('\u27A1 Preparation') {
      git 'git@github.com:openwms/org.openwms.tms.transportation.git'
      mvnHome = tool 'M3'
    }
    stage('\u27A1 Build & Deploy') {
      configFileProvider(
          [configFile(fileId: 'maven-local-settings', variable: 'MAVEN_SETTINGS')]) {
            sh "'${mvnHome}/bin/mvn' -s $MAVEN_SETTINGS clean install -Ddocumentation.dir=${WORKSPACE} -Dverbose=false -Psordocs,sonatype -U"
      }
    }
    stage('\u27A1 Results') {
      archive '**/target/*.jar'
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
    stage('\u27A1 Documentation') {
      configFileProvider(
          [configFile(fileId: 'maven-local-settings', variable: 'MAVEN_SETTINGS')]) {
            sh "'${mvnHome}/bin/mvn' -s $MAVEN_SETTINGS site site:deploy -Ddocumentation.dir=${WORKSPACE} -Dverbose=false -Psordocs,sonatype"
      }
    }
    stage('\u27A1 Sonar') {
      sh "'${mvnHome}/bin/mvn' clean org.jacoco:jacoco-maven-plugin:prepare-agent sonar:sonar -Djacoco.propertyName=jacocoArgLine -Dbuild.number=${BUILD_NUMBER} -Dbuild.date=${BUILD_ID} -Ddocumentation.dir=${WORKSPACE} -Pjenkins"
    }
  } finally {
    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
  }
}

