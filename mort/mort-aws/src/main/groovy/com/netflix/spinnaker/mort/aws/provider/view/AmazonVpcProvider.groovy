/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.mort.aws.provider.view

import com.amazonaws.services.ec2.model.Vpc
import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.cats.cache.Cache
import com.netflix.spinnaker.cats.cache.CacheData
import com.netflix.spinnaker.cats.cache.RelationshipCacheFilter
import com.netflix.spinnaker.mort.aws.cache.Keys
import com.netflix.spinnaker.mort.aws.model.AmazonVpc
import com.netflix.spinnaker.mort.model.VpcProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import static com.netflix.spinnaker.mort.aws.cache.Keys.Namespace.VPCS

@Component
class AmazonVpcProvider implements VpcProvider<AmazonVpc> {

  private static final String NAME_TAG_KEY = 'Name'

  private final Cache cacheView
  private final ObjectMapper objectMapper

  @Autowired
  AmazonVpcProvider(Cache cacheView, ObjectMapper objectMapper) {
    this.cacheView = cacheView
    this.objectMapper = objectMapper
  }

  @Override
  Set<AmazonVpc> getAll() {
    cacheView.getAll(VPCS.ns, RelationshipCacheFilter.none()).collect(this.&fromCacheData)
  }

  AmazonVpc fromCacheData(CacheData cacheData) {
    def parts = Keys.parse(cacheData.id)
    def vpc = objectMapper.convertValue(cacheData.attributes, Vpc)
    def tag = vpc.tags.find { it.key == NAME_TAG_KEY }
    String name = tag?.value
    new AmazonVpc(id: vpc.vpcId,
      name: name,
      account: parts.account,
      region: parts.region
    )
  }
}