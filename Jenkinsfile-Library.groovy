@Library('security')
	import scanSRCCLR
	import scanFortify
	import sendEmail
	pipeline {
	  agent any
	  triggers {
	    pollSCM('H * * * *')
	  }
	  options { timestamps() }
	  tools {
	    nodejs 'NodeJS_6.5.0'
	  }
	  environment {
	    // These variables are accessible globally
	    GLOBAL_VAR="example_value"
	  }
	  stages {
	    stage('srcclr') {
	      steps {
	        scanSRCCLR()
	      }
	    }
	    stage('fortify') {
	      environment {
	        // These variables are accessible to the fortify stage
	        STAGE_VAR="example_value"
	        //WGET_ARTIFACTS_SERVER=""
	        //WGET_ARTIFACTS_JOB_NAME=""
	        //WGET_SHA1_FILE=""
	        //WGET_ARCHIVE_FILE=""
	      }
	      steps {
	        scanFortify()
	      }
	    }
	    stage('email') {
	      steps {
	        sendEmail()
	      }
	    }
	  }
	}
