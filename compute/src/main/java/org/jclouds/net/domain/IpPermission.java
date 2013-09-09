/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.net.domain;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Range.closed;
import static com.google.common.collect.Range.singleton;
import static org.jclouds.util.Strings2.isCidrFormat;

import java.util.Set;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeRangeSet;

/**
 * Ingress access to a destination protocol on particular ports by source, which could be an ip
 * range (cidrblock), set of explicit security group ids in the current tenant, or security group
 * names in another tenant.
 * 
 * @author Adrian Cole
 * @see IpPermissions
 */
@Beta
public class IpPermission implements Comparable<IpPermission> {
   public static Builder builder() {
      return new Builder();
   }

   public static class Builder {
      private IpProtocol ipProtocol;
      private int fromPort;
      private int toPort;
      private RangeSet<Integer> ports = TreeRangeSet.create();
      private Multimap<String, String> tenantIdGroupNamePairs = LinkedHashMultimap.create();
      private Set<String> groupIds = Sets.newLinkedHashSet();
      private Set<String> cidrBlocks = Sets.newLinkedHashSet();

      /**
       * 
       * @see IpPermission#getIpProtocol()
       */
      public Builder ipProtocol(IpProtocol ipProtocol) {
         this.ipProtocol = ipProtocol;
         return this;
      }

      /**
       * 
       * @see IpPermission#getFromPort()
       */
      public Builder fromPort(int fromPort) {
         this.fromPort = fromPort;
         return this;
      }

      /**
       * 
       * @see IpPermission#getToPort()
       */
      public Builder toPort(int toPort) {
         this.toPort = toPort;
         return this;
      }

      /**
       * @see IpPermission#getTenantIdGroupNamePairs()
       */
      public Builder tenantIdGroupNamePair(String tenantId, String groupName) {
         this.tenantIdGroupNamePairs.put(tenantId, groupName);
         return this;
      }

      /**
       * @see IpPermission#getTenantIdGroupNamePairs()
       */
      public Builder tenantIdGroupNamePairs(Multimap<String, String> tenantIdGroupNamePairs) {
         this.tenantIdGroupNamePairs.putAll(tenantIdGroupNamePairs);
         return this;
      }

      /**
       * @see IpPermission#getCidrBlocks()
       */
      public Builder cidrBlock(String cidrBlock) {
         checkArgument(isCidrFormat(cidrBlock), "cidrBlock %s is not a valid CIDR", cidrBlock);
         this.cidrBlocks.add(cidrBlock);
         return this;
      }

      /**
       * @see IpPermission#getCidrBlocks()
       */
      public Builder cidrBlocks(Iterable<String> cidrBlocks) {
         for (String cidrBlock : cidrBlocks) {
            checkArgument(isCidrFormat(cidrBlock), "%s is not a valid CIDR", cidrBlock);
         }
         Iterables.addAll(this.cidrBlocks, cidrBlocks);
         return this;
      }

      /**
       * @see IpPermission#getPorts()
       */
      public Builder addPort(Integer port) {
         this.ports.add(singleton(checkNotNull(port, "port")));
         return this;
      }

      /**
       * @see IpPermission#getPorts()
       */
      public Builder addPortRange(Integer start, Integer end) {
         checkArgument(checkNotNull(start, "start") < checkNotNull(end, "end"),
                 "start of range must be lower than end of range but was [%d, %d]", start, end);
         this.ports.add(closed(start, end));
         return this;
      }

      /**
       * @see IpPermission#getPorts()
       */
      public Builder ports(RangeSet<Integer> ports) {
         this.ports = TreeRangeSet.create();
         this.ports.addAll(ports);
         return this;
      }

      /**
       * @see IpPermission#getGroupIds()
       */
      public Builder groupId(String groupId) {
         this.groupIds.add(groupId);
         return this;
      }

      /**
       * @see IpPermission#getGroupIds()
       */
      public Builder groupIds(Iterable<String> groupIds) {
         Iterables.addAll(this.groupIds, groupIds);
         return this;
      }

      public IpPermission build() {
         return new IpPermission(ipProtocol, fromPort, toPort, tenantIdGroupNamePairs, groupIds, cidrBlocks, ports);
      }
   }

   private final int fromPort;
   private final int toPort;
   private final Multimap<String, String> tenantIdGroupNamePairs;
   private final Set<String> groupIds;
   private final IpProtocol ipProtocol;
   private final Set<String> cidrBlocks;
   private final RangeSet<Integer> ports;

   public IpPermission(IpProtocol ipProtocol, int fromPort, int toPort,
                       Multimap<String, String> tenantIdGroupNamePairs, Iterable<String> groupIds,
                       Iterable<String> cidrBlocks) {
      this(ipProtocol, fromPort, toPort, tenantIdGroupNamePairs, groupIds, cidrBlocks,
              TreeRangeSet.<Integer>create());
   }

   public IpPermission(IpProtocol ipProtocol, int fromPort, int toPort,
            Multimap<String, String> tenantIdGroupNamePairs, Iterable<String> groupIds,
            Iterable<String> cidrBlocks, RangeSet<Integer> ports) {
      this.fromPort = fromPort;
      this.toPort = toPort;
      this.tenantIdGroupNamePairs = ImmutableMultimap.copyOf(checkNotNull(tenantIdGroupNamePairs,
               "tenantIdGroupNamePairs"));
      this.ipProtocol = checkNotNull(ipProtocol, "ipProtocol");
      this.groupIds = ImmutableSet.copyOf(checkNotNull(groupIds, "groupIds"));
      this.cidrBlocks = ImmutableSet.copyOf(checkNotNull(cidrBlocks, "cidrBlocks"));
      this.ports = ImmutableRangeSet.copyOf(checkNotNull(ports, "ports"));
   }

   /**
    * {@inheritDoc}
    */
   public int compareTo(IpPermission o) {
      return (this == o) ? 0 : getIpProtocol().compareTo(o.getIpProtocol());
   }

   /**
    * destination IP protocol
    */
   public IpProtocol getIpProtocol() {
      return ipProtocol;
   }

   /**
    * Start of destination port range for the TCP and UDP protocols, or an ICMP type number. An ICMP
    * type number of -1 indicates a wildcard (i.e., any ICMP type number).
    */
   public int getFromPort() {
      return fromPort;
   }

   /**
    * End of destination port range for the TCP and UDP protocols, or an ICMP code. An ICMP code of
    * -1 indicates a wildcard (i.e., any ICMP code).
    */
   public int getToPort() {
      return toPort;
   }

   /**
    * source of traffic allowed is on basis of another group in a tenant, as opposed to by cidr
    */
   public Multimap<String, String> getTenantIdGroupNamePairs() {
      return tenantIdGroupNamePairs;
   }

   /**
    * source of traffic allowed is on basis of another groupid in the same tenant
    */
   public Set<String> getGroupIds() {
      return groupIds;
   }

   /**
    * source of traffic is a cidrRange
    */
   public Set<String> getCidrBlocks() {
      return cidrBlocks;
   }

   /**
    * Each entry must be either an integer or a range. If not specified, connections through any port are allowed.
    * Example inputs include: ["22"], ["80,"443"], and ["12345-12349"].
    * <p/>
    * It is an error to specify this for any protocol that isn't UDP or TCP.
    *
    * @return A RangeSet covering ports which are allowed. Can be null.
    */
   public RangeSet<Integer> getPorts() {
      return ports;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      // allow subtypes
      if (o == null || !(o instanceof IpPermission))
         return false;
      IpPermission that = IpPermission.class.cast(o);
      return equal(this.ipProtocol, that.ipProtocol) && equal(this.fromPort, that.fromPort)
              && equal(this.toPort, that.toPort) && equal(this.tenantIdGroupNamePairs, that.tenantIdGroupNamePairs)
              && equal(this.groupIds, that.groupIds) && equal(this.cidrBlocks, that.cidrBlocks)
              && equal(this.ports, that.ports);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(ipProtocol, fromPort, toPort, tenantIdGroupNamePairs, groupIds, cidrBlocks, ports);
   }

   @Override
   public String toString() {
      return string().toString();
   }

   protected ToStringHelper string() {
      return Objects.toStringHelper("").add("ipProtocol", ipProtocol).add("fromPort", fromPort).add("toPort", toPort)
              .add("tenantIdGroupNamePairs", tenantIdGroupNamePairs).add("groupIds", groupIds)
              .add("cidrBlocks", cidrBlocks).add("ports", ports);
   }

}
