// project should be the last token of the Git repo URL in lowercase.
def project = "ncaaf"
def branch = BRANCH_NAME.toLowerCase()
def port = "9020"

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
        registryCredential = 'dockerhub'
      }
      steps {
        sh 'ls -l target'
        script {
          image = docker.build("$project/$branch:$BUILD_NUMBER \
              --label app.name=$project \
              --label branch=$branch")
        }
        // Cleanup previous images older than 12 hours
        sh "docker image prune -af \
              --filter label=app.name=$project \
              --filter label=branch=$branch \
              --filter until=12h"
      }
    }

    stage('Start New Docker') {
      steps {
        sh "docker run -d -p $port " +
            '--restart=always ' +
            '--dns=172.17.0.1 ' +
            "--name $project-$branch-$BUILD_NUMBER " +
            "$project/$branch:$BUILD_NUMBER"
      }
    }

    stage('Test New Docker') {
      steps {
        sh "sleep 10"
        script {
          ip = sh(
              returnStdout: true,
              script: "docker inspect $project-$branch-$BUILD_NUMBER | jq '.[].NetworkSettings.Networks.bridge.IPAddress'"
          )
          // Test new Docker instance directly
          url = ip.trim() + ":$port"
          sh "curl -f ${url}/application.wadl > /dev/null"
        }
      }
    }

    stage('Register New Service') {
      steps {
        script {
          consul = "http://127.0.0.1:8500/v1/agent/service/register"
          ip = sh(
              returnStdout: true,
              script: "docker inspect $project-$branch-$BUILD_NUMBER | jq '.[].NetworkSettings.Networks.bridge.IPAddress'"
          )
          def service = readJSON text: "{ \"Port\": $port }"
          service["Address"] = ip.toString().trim() replaceAll("\"", "");
          service["Name"] = "$project-$branch".toString()
          writeJSON file: 'service.json', json: service, pretty: 3
          sh(script: "cat service.json")
          sh(script: "curl -X PUT -d @service.json " + consul)
        }
      }
    }

    stage('Stop Previous Docker') {
      steps {
        script {
          // Get all matching containers except the most recent one
          containers = sh(
              returnStdout: true,
              script: "docker ps -q --filter label=branch=$branch --filter label=app.name=$project | tail -n+2"
          )
          containers = containers.trim()
          containers = containers.replace("\n", " ").replace("\r", " ");
          if(!containers.isEmpty()) {
            sh "docker stop $containers"
            sh "docker rm $containers"
          }
        }
      }
    }

    stage('Test Branch Path') {
      steps {
        // Test that NGINX regex location resolves correctly
        sh "curl -f localhost/$project/$branch/application.wadl"
      }
    }
  }

  post {
    always {
      // Cleanup Jenkins workspace
      cleanWs()
    }
  }

}
