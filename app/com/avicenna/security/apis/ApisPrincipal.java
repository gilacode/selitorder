/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.avicenna.security.apis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ApisPrincipal implements Serializable, Principal {

  private static final long serialVersionUID = 1L;

  private String name;

  private Collection<String> roles;

  private Collection<String> groups;

  private boolean adminPrincipal;

  private Map<String, String> attributes;

  public ApisPrincipal() {
    super();
  }

  public ApisPrincipal(String username) {
    this(username, new ArrayList<String>());
  }

  public ApisPrincipal(String username, Collection<String> roles) {
    this(username, roles, new HashMap<String, String>());
  }

  public ApisPrincipal(String username, Collection<String> roles, Map<String, String> attributes) {
    this(username, roles, attributes, new ArrayList<String>());
  }

  public ApisPrincipal(String username, Collection<String> roles, Map<String, String> attributes, Collection<String> groups) {
    this(username, roles, attributes, groups, false);
  }

  public ApisPrincipal(String username, Collection<String> roles, Map<String, String> attributes, Collection<String> groups, boolean adminPrincipal) {
    this.name = username;
    this.roles = roles;
    this.attributes = attributes;
    this.groups = groups;
    this.adminPrincipal = adminPrincipal;
  }

  public Collection<String> getRoles() {
    return roles;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public String getAttribute(String key) {
    if (attributes == null) {
      return null;
    }
    return attributes.get(key);
  }

  public void addAttribute(String key, String value) {
    if (attributes == null) {
      attributes = new HashMap<String, String>();
    }
    attributes.put(key, value);
  }

  public void addGroup(String name) {
    if (groups == null) {
      groups = new ArrayList<String>();
    }
    groups.add(name);
  }

  @Override
  public String getName() {
    return name;
  }

  @JsonIgnore
  public String getDisplayName() {
    return name;
  }

  @Override
  public String toString() {
    return getClass().getName() + " [name=" + name + ", roles=" + roles + ", attributes=" + attributes + "]";
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setRoles(Collection<String> roles) {
    this.roles = roles;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  public Collection<String> getGroups() {
    return groups;
  }

  public void setGroups(Collection<String> groups) {
    this.groups = groups;
  }

  @JsonIgnore
  public boolean isGroupAware() {
    return !CollectionUtils.isEmpty(groups);
  }

  public boolean isAdminPrincipal() {
    return adminPrincipal;
  }

  public void setAdminPrincipal(boolean adminPrincipal) {
    this.adminPrincipal = adminPrincipal;
  }
}
