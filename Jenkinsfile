def dirProject 
def app
def pom
def version
def majorVersion

pipeline {
	agent any

	parameters {
        string(name: 'dirProject', defaultValue: 'ODSDataMartExtractor', description: 'Cartella effettiva del progetto')
        string(name: 'dockerRegistry', defaultValue: 'tcp://192.168.99.121:2376', description: 'Docker registry')
		string(name: 'dockerHubUser', defaultValue: 'lucacelardo', description: 'Docker Hub username')
		string(name: 'dockerHubPass', defaultValue: '', description: 'Docker Hub password')
		string(name: 'namespaceService', defaultValue: 'bu-arc', description: 'Namespace')
		string(name: 'containerPort', defaultValue: '8080', description: 'Container service port')
		string(name: 'contextRoot', defaultValue: '/nexi-extractor', description: 'Application context root')
		booleanParam(name: 'deployApplication', defaultValue: false, description: 'True if you want to deploy the application')
		//booleanParam(name: 'deployConfigMap', defaultValue: false, description: 'True only if you want to update ConfigMap')
		booleanParam(name: 'deployService', defaultValue: false, description: 'True only for the First Deploy')
		booleanParam(name: 'deployIngress', defaultValue: false, description: 'True only for the First Deploy')
	}

	environment {
		kubeEndpoint = "-:-"
		kubeUser = "-"
	}


	stages {
		stage('Source Checkout') {
			steps {
				script {
					dirProject = "${params.dirProject}";
					git branch: "master", url: "https://github.com/Luca-Celardo/UseCaseNEXI.git"
				}
			}
		}
		stage('Build Application') {
			steps {
				script {
					dir("${dirProject}") {
						bat "mvn -B clean install -DskipTests=true"
						pom = readMavenPom file: 'pom.xml'
						version = pom.version
                    	majorVersion = version.tokenize('.')[0]
					}
				}
			}
		}
		stage('Archive Application') {
			steps {
				script {
					dir("${dirProject}") {
						bat "mvn -B package -DskipTests=true"
					}
				}
			}
		}
		stage('Build Image') {
			steps {
				script {
					dir("${dirProject}") {
						bat "docker build -t lucacelardo/odsdatamartextractor:${pom.version} -f Dockerfile ."
						//bat "docker login -u ${params.dockerHubUser} -p ${params.dockerHubPass} docker.io"
						bat "docker login -u ${params.dockerHubUser} --password-stdin docker.io"
						bat "docker push lucacelardo/odsdatamartextractor:${pom.version}"
					}
				}
			}
		}
	}
}