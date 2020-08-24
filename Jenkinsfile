// project should be the last token of the Git repo URL in lowercase.
def project = "ncaaf"
def branch = BRANCH_NAME.toLowerCase()
def port = "9020"
def svcName = project + "-" + branch
// Also hostname
def svcId = svcName + "-" + BUILD_NUMBER

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
      // Also registers hostname with Consul.io
      steps {
        sh "docker run -d -p $port " +
            "-e SERVICE=$svcName " +
            '--restart=always ' +
            '--dns=172.17.0.1 ' +
            "--name $svcId " +
            "--hostname $svcId " +
            "$project/$branch:$BUILD_NUMBER"
      }
    }

    stage('Test/Register New Docker') {
      steps {
        sh "sleep 10"
        script {
          // Test new Docker instance directly
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
