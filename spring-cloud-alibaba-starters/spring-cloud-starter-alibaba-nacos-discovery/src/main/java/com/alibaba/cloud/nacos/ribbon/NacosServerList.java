/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos.ribbon;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;

/**
 * @author xiaojing
 * @author renhaojun
 *
 * @see com.netflix.loadbalancer.ServerList Nacos服务发现组件
 */
public class NacosServerList extends AbstractServerList<NacosServer> {

	private NacosDiscoveryProperties discoveryProperties;

	private String serviceId;

	public NacosServerList(NacosDiscoveryProperties discoveryProperties) {
		this.discoveryProperties = discoveryProperties;
	}

	@Override
	public List<NacosServer> getInitialListOfServers() {
		return getServers();
	}

	@Override
	public List<NacosServer> getUpdatedListOfServers() {
		return getServers();
	}

	/**
	 * 从负载均衡ribbon组件代码跟踪过来，最后落到了getServers()方法
	 * @return
	 */
	private List<NacosServer> getServers() {
		try {
			String group = discoveryProperties.getGroup();
			// NamingService -》 NacosNamingService
			// 获取服务对应的所有可用实例
			List<Instance> instances = discoveryProperties.namingServiceInstance()
					// 获取所有实例
					.selectInstances(serviceId, group, true);
			// 根据Instance List返回NacosServer List
			return instancesToServerList(instances);
		}
		catch (Exception e) {
			throw new IllegalStateException(
					"Can not get service instances from nacos, serviceId=" + serviceId,
					e);
		}
	}

	private List<NacosServer> instancesToServerList(List<Instance> instances) {
		List<NacosServer> result = new ArrayList<>();
		if (CollectionUtils.isEmpty(instances)) {
			return result;
		}
		for (Instance instance : instances) {
			result.add(new NacosServer(instance));
		}

		return result;
	}

	public String getServiceId() {
		return serviceId;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig iClientConfig) {
		this.serviceId = iClientConfig.getClientName();
	}

}
