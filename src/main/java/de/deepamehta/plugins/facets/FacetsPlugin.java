package de.deepamehta.plugins.facets;

import de.deepamehta.plugins.facets.service.FacetsService;

import de.deepamehta.core.AssociationDefinition;
import de.deepamehta.core.Topic;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.CompositeValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.model.TopicRoleModel;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.Directives;
import de.deepamehta.core.service.Plugin;

import java.util.logging.Logger;



public class FacetsPlugin extends Plugin implements FacetsService {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods



    // ************************************
    // *** FacetsService Implementation ***
    // ************************************



    @Override
    public void associateWithFacetType(long topicId, String facetTypeUri) {
        dms.createAssociation(new AssociationModel("dm4.core.instantiation", 
            new TopicRoleModel(topicId,      "dm4.core.instance"),
            new TopicRoleModel(facetTypeUri, "dm4.facets.facet")), null);   // clientState=null
    }

    @Override
    public Topic addFacet(Topic topic, String facetTypeUri, TopicModel facet, ClientState clientState,
                                                                              Directives directives) {
        AssociationDefinition assocDef = getAssocDef(facetTypeUri);
        Topic childTopic = fetchChildTopic(topic, assocDef);
        if (childTopic != null) {
            childTopic.update(facet, clientState, directives);
        } else {
            // Note: the type URI of a simplified topic model (as constructed from update requests) is not initialzed.
            String childTopicTypeUri = assocDef.getPartTopicTypeUri();
            facet.setTypeUri(childTopicTypeUri);
            // create and associate child topic
            childTopic = dms.createTopic(facet, null);
            associateChildTopic(topic, assocDef, childTopic.getId());
        }
        return childTopic;
    }

    @Override
    public Topic getFacet(Topic topic, String facetTypeUri) {
        // ### TODO: integrity check: is the topic an instance of that facet type?
        // ### TODO: many cardinality
        return fetchChildTopic(topic, getAssocDef(facetTypeUri));
    }



    // ------------------------------------------------------------------------------------------------- Private Methods

    private AssociationDefinition getAssocDef(String facetTypeUri) {
        // Note: a facet type has exactly *one* association definition
        return dms.getTopicType(facetTypeUri, null).getAssocDefs().values().iterator().next();
    }

    /**
     * Fetches and returns a child topic or <code>null</code> if no such topic extists.
     * <p>
     * Note: There is a principal copy in AttachedDeepaMehtaObject but here the precondition is different:
     * The given AssociationDefinition is not necessarily part of the given topic's type.
     */
    private Topic fetchChildTopic(Topic topic, AssociationDefinition assocDef) {
        String assocTypeUri       = assocDef.getInstanceLevelAssocTypeUri();
        String myRoleTypeUri      = assocDef.getWholeRoleTypeUri();
        String othersRoleTypeUri  = assocDef.getPartRoleTypeUri();
        String othersTopicTypeUri = assocDef.getPartTopicTypeUri();
        //
        return topic.getRelatedTopic(assocTypeUri, myRoleTypeUri, othersRoleTypeUri, othersTopicTypeUri, true, false);
        // fetchComposite=true ### FIXME: make fetchComposite a parameter
    }

    /**
     * Note: There is a principal copy in AttachedDeepaMehtaObject but here the precondition is different:
     * The given AssociationDefinition is not necessarily part of the given topic's type.
     */
    private void associateChildTopic(Topic topic, AssociationDefinition assocDef, long childTopicId) {
        dms.createAssociation(new AssociationModel(assocDef.getInstanceLevelAssocTypeUri(),
            new TopicRoleModel(topic.getId(), assocDef.getWholeRoleTypeUri()),
            new TopicRoleModel(childTopicId,  assocDef.getPartRoleTypeUri())), null);   // clientState=null
    }
}
