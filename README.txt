# Copyright 2009 Hippo (www.onehippo.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Querying? See pom runner.properties and pom.xml

# build & run
mvn clean compile exec:java

# rerun (faster)
mvn -o -q compile exec:java

# create app
mvn clean package appassembler:assemble
sh target/jcr-runner/bin/jcr-runner
