/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.routing.util;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.profiles.*;
import com.graphhopper.storage.*;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class ConditionalAccessTest {
    private static final String CONDITIONAL = "no @ (Mar 15-Jun 15)";
    private final CarFlagEncoder encoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(encoder);
    private final GraphHopperStorage graph = new GraphBuilder(encodingManager).create();

    private ReaderWay createWay() {
        ReaderWay way = new ReaderWay(0);
        way.setTag("highway", "primary");
        return way;
    }

    private EdgeIteratorState createEdge(ReaderWay way) {
        EncodingManager.AcceptWay acceptWay = new EncodingManager.AcceptWay();
        encodingManager.acceptWay(way, acceptWay);
        IntsRef flags = encodingManager.handleWayTags(way, acceptWay , 0);
        EdgeIteratorState edge = graph.edge(0, 1).setFlags(flags);
        encodingManager.applyWayTags(way, edge);
        return edge;
    }

    @Test
    public void isAccessConditional() {
        ReaderWay way = createWay();
        assertFalse(encoder.getAccess(way).isConditional());
        way.setTag("access:conditional", CONDITIONAL);
        assertTrue(encoder.getAccess(way).isConditional());
    }

    @Test
    public void setConditionalBit() {
        ReaderWay way = createWay();
        BooleanEncodedValue conditionalEnc = encodingManager.getBooleanEncodedValue(EncodingManager.getKey(encoder, "conditional_access"));
        assertFalse(createEdge(way).get(conditionalEnc));
        way.setTag("access:conditional", CONDITIONAL);
        assertTrue(createEdge(way).get(conditionalEnc));
    }

    @Test
    public void setConditionalValue() {
        ReaderWay way = createWay();
        way.setTag("access:conditional", CONDITIONAL);
        EncodingManager.AcceptWay acceptWay = new EncodingManager.AcceptWay();
        encodingManager.acceptWay(way, acceptWay);
        IntsRef flags = encodingManager.handleWayTags(way, acceptWay , 0);
        EdgeIteratorState edge = graph.edge(0, 1).setFlags(flags);
        // store conditional
        List<EdgeIteratorState> createdEdges = new ArrayList<>();
        createdEdges.add(edge);
        ConditionalEdgesMap conditionalEdges = graph.getConditionalAccess(encoder);
        conditionalEdges.addEdges(createdEdges, encoder.getConditionalTagInspector().getTagValue());
        assertEquals(CONDITIONAL, conditionalEdges.getValue(edge.getEdge()));
    }

}