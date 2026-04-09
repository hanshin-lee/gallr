-- Create a public Storage bucket for user avatar images.

INSERT INTO storage.buckets (id, name, public)
VALUES ('avatars', 'avatars', true)
ON CONFLICT (id) DO NOTHING;

-- Anyone can view avatars (public bucket)
CREATE POLICY "Public read avatars"
  ON storage.objects FOR SELECT
  USING (bucket_id = 'avatars');

-- Authenticated users can upload their own avatar (path = userId.jpg)
CREATE POLICY "Owner upload avatar"
  ON storage.objects FOR INSERT
  WITH CHECK (
    bucket_id = 'avatars'
    AND auth.uid() IS NOT NULL
    AND (storage.foldername(name))[1] IS NULL
    AND name = (auth.uid()::text || '.jpg')
  );

-- Authenticated users can update (overwrite) their own avatar
CREATE POLICY "Owner update avatar"
  ON storage.objects FOR UPDATE
  USING (
    bucket_id = 'avatars'
    AND auth.uid() IS NOT NULL
    AND name = (auth.uid()::text || '.jpg')
  );
