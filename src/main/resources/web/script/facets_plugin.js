function facets_plugin() {

    // ------------------------------------------------------------------------------------------------------ Public API

    /**
     * Extends the page model of the webclient's default topic renderer by facets.
     * Utility method to be called by other plugins.
     *
     * @param   topic           The facetted topic the page/form is rendered for. Usually that is the selected topic.
     * @param   facet_types     The facet types to add to the page model (array of topic objects of type "Topic Type").
     * @param   page_model      The page model to extend.
     * @param   setting         "viewable" or "editable"
     */
    this.add_facets_to_page_model = function(topic, facet_types, page_model, setting) {
        for (var i = 0; i < facet_types.length; i++) {
            var facet_type = dm4c.get_topic_type(facet_types[i].uri)
            var assoc_def = facet_type.assoc_defs[0]
            var topic_type = dm4c.get_topic_type(assoc_def.part_topic_type_uri)
            var field_uri = dm4c.COMPOSITE_PATH_SEPARATOR + assoc_def.uri
            var value_topic = topic.composite[assoc_def.uri]
            var fields = TopicRenderer.create_fields(topic_type, assoc_def, field_uri, value_topic, topic, setting)
            page_model[assoc_def.uri] = fields
        }
    }
}
