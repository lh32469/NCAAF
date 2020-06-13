// project should be the last token of the Git repo URL in lowercase.
def project = "ncaaf"
def branch = BRANCH_NAME.toLowerCase()

pipeline {

  options {
    // Discard everything except the last 10 builds
    buildDiscarder(logRotator(numToKeepStr: '10'))
    // Don't build the same branch concurrently
    disableConcurrentBuilds()

    // Cleanup orphaned branch Docker container
    branchTearDownExecutor 'CleanupDocker'
  }

  agent any

  stages {

    stage('Compile') {
      agent {
        docker {
          reuseNode true
          image 'maven:latest'
          args '-u root -v /var/lib/jenkins/.m2:/root/.m2'
        }
      }
      steps {
        sh 'mvn -B -DskipTests clean compile'
      }
    }

    stage('Test') {
      agent {
        docker {
          reuseNode true
          image 'maven:latest'
          args '--dns=172.17.0.1 -u root -v /var/lib/jenkins/.m2:/root/.m2'
        }
      }
      steps {
        sh 'mvn -B package'
        junit '**/target/surefire-reports/TEST-*.xml'
      }
    }

    stage('Build New Docker') {
      environment {
        registry = "$project/$branch"
        registryCredential = 'dockerhub'
      }
      steps {
        sh 'ls -l target'
        script {
          image = docker.build registry + ":$BUILD_NUMBER"
        }
        // Cleanup Maven target directory
        sh 'rm -rf target'
      }
    }

    stage('Stop Existing Docker') {
      steps {
        sh "docker stop $project-$branch || true && docker rm $project-$branch || true"
      }
    }

    stage('Start New Docker') {
      steps {
        sh 'docker run -d -p 9020 ' +
            '--restart=always ' +
            '--dns=172.17.0.1 ' +
            "--name $project-$branch " +
            "$project/$branch:$BUILD_NUMBER"
      }
    }

    stage('Register Consul Service') {
      steps {
        script {
          consul = "http://127.0.0.1:8500/v1/agent/service/register"
          ip = sh(
              returnStdout: true,
              script: "docker inspect $project-$branch | jq '.[].NetworkSettings.Networks.bridge.IPAddress'"
          )
          def service = readJSON text: '{ "Port": 9020 }'
          service["Address"] = ip.toString().trim() replaceAll("\"", "");
          service["Name"] = "$project-$branch".toString()
          writeJSON file: 'service.json', json: service, pretty: 3
          sh(script: "cat service.json")
          sh(script: "curl -X PUT -d @service.json " + consul)
        }
      }
    }

    stage('Test New Branch') {
      steps {
        sh "sleep 15"
        sh "curl -f localhost/$project/$branch/application.wadl"
      }
    }

  }

}
