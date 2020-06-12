/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.connect.mirror;

import org.apache.kafka.common.Configurable;

import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Defines remote topics like "us-west.topic1". The separator is customizable and defaults to a period. */
public class DefaultReplicationPolicy implements ReplicationPolicy, Configurable {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultReplicationPolicy.class);

    // In order to work with various metrics stores, we allow custom separators.
    public static final String SEPARATOR_CONFIG = MirrorClientConfig.REPLICATION_POLICY_SEPARATOR;
    public static final String SEPARATOR_DEFAULT = ".";
    public static final String SUFFIX = "mirror";

    private String separator = SEPARATOR_DEFAULT;
    private Pattern separatorPattern = Pattern.compile(Pattern.quote(SEPARATOR_DEFAULT));

    @Override
    public void configure(Map<String, ?> props) {
        if (props.containsKey(SEPARATOR_CONFIG)) {
            separator = (String) props.get(SEPARATOR_CONFIG);
            log.info("Using custom remote topic separator: '{}'", separator);
            separatorPattern = Pattern.compile(Pattern.quote(separator));
        }
    }

    @Override
    public String formatRemoteTopic(String sourceClusterAlias, String topic) {
        return sourceClusterAlias + separator + topic + separator + SUFFIX;
    }

    @Override
    public String topicSource(String topic) {
        String[] parts = separatorPattern.split(topic);
        if (parts.length < 2) {
            // this is not a remote topic
            return null;
        } else {
            return parts[0];
        }
    }

    @Override
    public String upstreamTopic(String topic) {
        String source = topicSource(topic);
        if (source == null) {
            return null;
        }
        else if (topic.contains(separator + SUFFIX)) {
            return null;
        } 
        else {
            return topic.substring(source.length() + separator.length(), topic.length() - SUFFIX.length() - 1);
        }
    }
}
