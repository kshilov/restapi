create unique index action_unique_offer_and_performer_for_active on action(offer_id, performer_id) where deleted = false;
