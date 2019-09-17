pipeline {
  agent any
  tools {
    nodejs 'NodeJS_6.5.0'
  }
  environment {
    VERSION_NAME="SOME-TEAM"
    SRCCLR_CI_JSON=1
    FORTIFY_DIR="$WORKSPACE/fortify"
    TRICODER_DIR="$WORKSPACE/tricoder"
    TRICODER_PATH="$TRICODER_DIR/scripts/java"
    TRICODER_SCRIPT="security.sh"
    EXCLUDE="-exclude $FORTIFY_DIR -exclude $TRICODER_DIR"
    LOCAL_NPM_CACHE="${WORKSPACE}/../${JOB_NAME}_LOCAL_NPM_CACHE"
    LOCAL_NPM_TMP="${WORKSPACE}/../${JOB_NAME}_LOCAL_NPM_TMP"
    SERVER="http://##.###.##.###:8080"
    BUILD_FILE="$WORKSPACE/build_security.sh"
    ORG="NameOfOrganization"
    COMPANY="NameOfCompany"
  }
  stages {
    stage('fetch_security') {
      steps {
        timestamps() {
          sh '''rm -rf $TRICODER_DIR $FORTIFY_DIR
          mkdir -pv $TRICODER_DIR $FORTIFY_DIR
          '''
          dir(path: 'tricoder') {
            git(url: 'git@github.$COMPANY.com:$ORG/tricocer-repo.git', branch: 'develop')
          }
          
          dir(path: 'fortify') {
            git 'git@github.$COMPANY:$ORG/jenkins-scripts.git'
          }
          
          sh '''ls -la $WORKSPACE $TRICODER_DIR $FORTIFY_DIR
          '''
        }
      }
    }
    stage('tricoder') {
      steps {
        timestamps() {
          withCredentials(bindings: [string(credentialsId: 'FORTIFY_AUTH_KEY', variable: 'FORTIFY_AUTH_KEY'), string(credentialsId: 'SRCCLR_API_TOKEN', variable: 'SRCCLR_API_TOKEN')]) {
            sh '''npm --version || true

            echo "***  CHANGE MODE FOR TRICODER SCRIPT  ***"
            chmod 775 $TRICODER_PATH/$TRICODER_SCRIPT $TRICODER_PATH/get_plist_version.sh

            echo "***  PREP FOR TRICODER SCRIPT  ***"
            curl -sSL https://download.sourceclear.com/ci.sh | sh > $VERSION_NAME.json

            echo "***  RUN TRICODER SCRIPT  ***"
            $TRICODER_PATH/$TRICODER_SCRIPT $ORG $VERSION_NAME $VERSION_NAME.json $SERVER
            '''
          }
        }
      }
    }
    stage('fortify') {
      steps {
        timestamps() {
          withCredentials(bindings: [string(credentialsId: 'FORTIFY_AUTH_KEY', variable: 'FORTIFY_AUTH_KEY'), string(credentialsId: 'SRCCLR_API_TOKEN', variable: 'SRCCLR_API_TOKEN')]) {
            sh '''set +x

            echo "***  IF EMBER THEN CALL BUILD FILE  ***"
            chmod 775 $BUILD_FILE
            $BUILD_FILE

            echo "***  RUN FORTIFY SCAN SCRIPT  ***"
            chmod 775 $FORTIFY_DIR/*scan*.sh || true
            $FORTIFY_DIR/scan_test.sh || true
            '''            
          }
        }
      }
    }
    stage('archive') {
      steps {
        timestamps() {
          archiveArtifacts(artifacts: '*.fpr, *.log, $ORG*.json', onlyIfSuccessful: true)
        }
      }
    }
    stage('email') {
      steps {
        emailext(subject: '$DEFAULT_SUBJECT', body: 'Build information is located here: ${BUILD_URL}  Build log is located here:  ${BUILD_URL}console  ${CAUSE}  <br>${CHANGES, showPaths=true}', replyTo: 'Build@$COMPANY.com', from: 'Build@$COMPANY.com', to: 'bcc:build-notifications@$COMPANY.com')
      }
    }
  }
}

