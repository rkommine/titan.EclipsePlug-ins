/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.graph.clustering.visualization.ClusterEdge;
import org.eclipse.titanium.graph.clustering.visualization.ClusterNode;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.preferences.PreferenceConstants;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class contains functions used in other clustering tools.
 * 
 * @author Gobor Daniel
 */
public class ClusteringTools {

	private static List<String> prefixes = createPrefix();
	
	static {
		final Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					final String property = event.getProperty();
					if (PreferenceConstants.CLUSTER_TRUNCATE.equals(property)) {
						prefixes = createPrefix();
						return;
					}
				}
			});
		}
	}

	private ClusteringTools() {
	}
	
	/**
	 * Creates the list of prefixes from the settings.
	 * 
	 * @return The list of prefixes
	 */
	private static List<String> createPrefix() {
		List<String> list = new ArrayList<String>();

		final String stringList = Platform.getPreferencesService().getString(Activator.PLUGIN_ID, PreferenceConstants.CLUSTER_TRUNCATE, "", null);
		final List<String> splittedList = ResourceExclusionHelper.intelligentSplit(stringList, '#', '\\');

		for (String item : splittedList) {
			list.add(item);
		}

		return list;
	}

	public static String truncate(String name) {
		String best = null;
		int size = 0;
		for (String s : prefixes) {
			if (name.startsWith(s) && s.length() > size) {
				size = s.length();
				best = s;
			}
		}

		if (best == null) {
			return name;
		}

		return name.replace(best, "...");
	}

	/**
	 * It creates the cluster graph from a module graph and the clusters with
	 * their names.
	 * 
	 * @param moduleGraph
	 *            The module graph
	 * @param mapNameCluster
	 *            The clusters mapped to their names
	 * @return The cluster graph
	 */
	public static DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> generateClusterGraph(
			DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> moduleGraph, Map<String, Set<NodeDescriptor>> mapNameCluster) {

		Map<Set<NodeDescriptor>, ClusterNode> mapClusterNode = new HashMap<Set<NodeDescriptor>, ClusterNode>();
		DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> clusterGraph = new DirectedSparseGraph<NodeDescriptor, EdgeDescriptor>();
		for (Entry<String, Set<NodeDescriptor>> entry : mapNameCluster.entrySet()) {  
			final String name = entry.getKey();
			final Set<NodeDescriptor> cluster = entry.getValue();
			for (NodeDescriptor v : cluster) {
				v.setCluster(cluster);
			}
			ClusterNode clusternode = new ClusterNode(name, cluster);
			clusterGraph.addVertex(clusternode);
			mapClusterNode.put(cluster, clusternode);
		}
		for (EdgeDescriptor e : moduleGraph.getEdges()) {
			final NodeDescriptor v = moduleGraph.getSource(e);
			final NodeDescriptor w = moduleGraph.getDest(e);
			Set<NodeDescriptor> clusterv = v.getCluster();
			Set<NodeDescriptor> clusterw = w.getCluster();
			if (clusterv == null || clusterw == null) {
				continue;
			}
			if (clusterv != clusterw) {
				ClusterNode clusterNodev = mapClusterNode.get(clusterv);
				ClusterNode clusterNodew = mapClusterNode.get(clusterw);
				if (clusterNodev == null || clusterNodew == null) {
					continue;
				}
				EdgeDescriptor ce = clusterGraph.findEdge(clusterNodev, clusterNodew);
				if (ce != null) {
					ce.setWeight((Integer) ce.getWeight() + 1);
				} else {
					ce = new ClusterEdge(clusterNodev.getName() + "-" + clusterNodew.getName(), 1);
					clusterGraph.addEdge(ce, clusterNodev, clusterNodew);
				}
			}
		}

		return clusterGraph;
	}

	/**
	 * It creates the cluster graph from a module graph and a given clustering.
	 * It will create numeric values for the cluster names.
	 * 
	 * @param moduleGraph
	 *            The module graph
	 * @param clusters
	 *            The clustering of the module graph
	 * @return The cluster graph
	 */
	public static DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> generateClusterGraph(
			DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> moduleGraph, Set<Set<NodeDescriptor>> clusters) {

		Map<String, Set<NodeDescriptor>> mapNameCluster = new HashMap<String, Set<NodeDescriptor>>();
		Integer i = 0;
		for (Set<NodeDescriptor> cluster : clusters) {
			++i;
			mapNameCluster.put(i.toString(), cluster);
		}

		return generateClusterGraph(moduleGraph, mapNameCluster);
	}
	
}
