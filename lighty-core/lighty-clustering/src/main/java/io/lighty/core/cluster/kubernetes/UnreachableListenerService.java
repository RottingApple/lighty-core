/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v10.html
 */
package io.lighty.core.cluster.kubernetes;

import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.cluster.Cluster;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.cluster.admin.rev151013.ClusterAdminService;

public class UnreachableListenerService implements ClusterSingletonService {
    private static final String CLUSTER_SINGLETON_ID = "singletonUnreachableListener";
    private static final String UNREACHABLE_LISTENER_ACTOR_NAME = "unreachableListener";

    private final Cluster cluster;
    private final Long podRestartTimeout;
    private final ActorSystem actorSystem;
    private final DataBroker dataBroker;
    private final ClusterAdminService clusterAdminRPCService;
    private ActorRef unreachableListener;
    private String kubernetesPodsNamespace;
    private String kubernetesPodsSelector;

    public UnreachableListenerService(ActorSystem actorSystem, DataBroker dataBroker,
                                      ClusterAdminService clusterAdminRPCService,
                                      String kubernetesPodsNamespace, String kubernetesPodsSelector,
                                      Long podRestartTimeout) {
        this.actorSystem = actorSystem;
        this.dataBroker = dataBroker;
        this.clusterAdminRPCService = clusterAdminRPCService;
        this.cluster = Cluster.get(actorSystem);
        this.podRestartTimeout = podRestartTimeout;
        this.kubernetesPodsNamespace = kubernetesPodsNamespace;
        this.kubernetesPodsSelector = kubernetesPodsSelector;
    }

    @Override
    public void instantiateServiceInstance() {
        this.unreachableListener = actorSystem.actorOf(UnreachableListener.props(actorSystem, dataBroker,
                clusterAdminRPCService, kubernetesPodsNamespace, kubernetesPodsSelector, podRestartTimeout),
                UNREACHABLE_LISTENER_ACTOR_NAME);
    }

    @Override
    public ListenableFuture<? extends Object> closeServiceInstance() {
        cluster.unsubscribe(this.unreachableListener);
        this.unreachableListener.tell(PoisonPill.getInstance(), this.unreachableListener);
        return Futures.immediateFuture(Done.done());
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return ServiceGroupIdentifier.create(CLUSTER_SINGLETON_ID);
    }
}
