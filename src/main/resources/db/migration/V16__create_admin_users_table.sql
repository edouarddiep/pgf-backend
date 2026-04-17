CREATE TABLE admin_users (
                             id           UUID PRIMARY KEY,
                             email        VARCHAR(255) NOT NULL UNIQUE,
                             display_name VARCHAR(100),
                             approved     BOOLEAN NOT NULL DEFAULT FALSE,
                             approved_at  TIMESTAMP,
                             created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE POLICY "owner_only" ON public.admin_users
  FOR ALL USING (auth.uid() = '892c889a-83a4-44e2-a171-0118d907773a'::uuid);
ALTER TABLE public.admin_users ENABLE ROW LEVEL SECURITY;