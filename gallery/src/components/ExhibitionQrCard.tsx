import { useEffect, useId, useState } from "react";
import type { OwnerExhibition } from "../domain";
import {
  createExhibitionQrArtwork,
  downloadExhibitionQrArtwork,
  type ExhibitionQrArtwork,
} from "../exhibitionQr";
import { localizeBilingual, useLocale } from "../i18n";
import { publicExhibitionUrl } from "../publicExhibitionUrl";

type ExhibitionIdentity = Pick<OwnerExhibition, "id" | "nameKo" | "nameEn">;
type QrState =
  | { status: "loading" }
  | { status: "ready"; artwork: ExhibitionQrArtwork }
  | { status: "error" };

export function ExhibitionQrCard({
  exhibition,
  posterUrl,
  publicSiteUrl,
}: {
  exhibition: ExhibitionIdentity;
  posterUrl: string | null;
  publicSiteUrl: string;
}) {
  const { locale, messages } = useLocale();
  const headingId = useId();
  const [attempt, setAttempt] = useState(0);
  const [state, setState] = useState<QrState>({ status: "loading" });
  const [downloaded, setDownloaded] = useState(false);
  const exhibitionUrl = publicExhibitionUrl(exhibition, publicSiteUrl);
  const exhibitionName = localizeBilingual(
    exhibition.nameKo,
    exhibition.nameEn,
    locale,
  );

  useEffect(() => {
    let current = true;
    setState({ status: "loading" });
    setDownloaded(false);
    void createExhibitionQrArtwork({ exhibitionUrl, posterUrl })
      .then((artwork) => {
        if (current) setState({ status: "ready", artwork });
      })
      .catch(() => {
        if (current) setState({ status: "error" });
      });
    return () => {
      current = false;
    };
  }, [attempt, exhibitionUrl, posterUrl]);

  const download = () => {
    if (state.status !== "ready") return;
    try {
      downloadExhibitionQrArtwork({
        svg: state.artwork.svg,
        exhibitionId: exhibition.id,
      });
      setDownloaded(true);
    } catch {
      setDownloaded(false);
      setState({ status: "error" });
    }
  };

  return (
    <section className="exhibition-qr-card" aria-labelledby={headingId}>
      <h2 id={headingId}>{messages.exhibitions.editor.qrTitle}</h2>
      <p>{messages.exhibitions.editor.qrBody}</p>
      {state.status === "loading" ? (
        <div className="exhibition-qr-placeholder" role="status">
          {messages.exhibitions.editor.qrGenerating}
        </div>
      ) : state.status === "error" ? (
        <div className="exhibition-qr-recovery">
          <p className="field-error" role="alert">
            ! {messages.exhibitions.editor.qrError}
          </p>
          <button
            className="outlined-button"
            type="button"
            onClick={() => setAttempt((current) => current + 1)}
          >
            {messages.exhibitions.editor.qrRetry}
          </button>
        </div>
      ) : (
        <>
          <img
            className="exhibition-qr-preview"
            src={state.artwork.previewUrl}
            alt={messages.exhibitions.editor.qrAlt(exhibitionName)}
            draggable={false}
          />
          <button
            className="outlined-button exhibition-qr-download"
            type="button"
            onClick={download}
          >
            {messages.exhibitions.editor.qrDownload}
          </button>
          {(downloaded || state.artwork.colorSource === "fallback") && (
            <p className="exhibition-qr-status" role="status">
              {downloaded
                ? messages.exhibitions.editor.qrDownloaded
                : messages.exhibitions.editor.qrFallback}
            </p>
          )}
        </>
      )}
    </section>
  );
}
