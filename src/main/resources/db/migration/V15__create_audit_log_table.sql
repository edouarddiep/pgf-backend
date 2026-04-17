CREATE TABLE audit_log (
                           id          BIGSERIAL PRIMARY KEY,
                           entity_type VARCHAR(50)  NOT NULL,
                           entity_id   BIGINT,
                           action      VARCHAR(20)  NOT NULL,
                           before_json TEXT,
                           after_json  TEXT,
                           performed_by VARCHAR(100),
                           ip_address  VARCHAR(45),
                           user_agent  VARCHAR(500),
                           created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at DESC);

CREATE POLICY "owner_only" ON public.audit_log
  FOR ALL USING (auth.uid() = '892c889a-83a4-44e2-a171-0118d907773a'::uuid);
ALTER TABLE public.audit_log ENABLE ROW LEVEL SECURITY;