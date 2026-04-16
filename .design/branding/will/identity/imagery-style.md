# Imagery Style
> Phase: identity | Brand: will | Generated: 2026-04-15

## The UI is the imagery

Will is a launcher. The primary visual artifact is the app itself — the task feed, the overlay, the app drawer. There is no illustration layer, no photography direction that carries brand meaning. The screenshots are the brand.

A polished device mockup around a Will screenshot would contradict the brand — it would make Will look like a lifestyle product. The correct reference is a well-composed README: tight crop, real data, dark background, no device chrome.

---

## Photography

**Style:** Not applicable for in-product use. For any external-facing assets (README, Play Store, press kit): screenshots only.

**Subjects:** The UI in real use states. Real task data — no lorem ipsum. Real Android notifications captured as tasks.

**Composition:**
- Tight crop on the specific interaction — show the task card, the overlay, the action sheet
- Single focused state per image
- True black `#000000` fills the entire frame edge to edge

**Color treatment:** No treatment applied to screenshots. The dark UI is the treatment. No vignette, no color overlay, no filter.

**Don'ts:**
- No device mockups (no phone frames, no Pixel/iPhone renders)
- No lifestyle photography (no hand holding phone over coffee table)
- No hand model shoots
- No "person looking focused at phone" stock photography

---

## Illustration

**Style:** None in product.

For documentation or error states: short lines of text in Will's voice. A sentence in Geist Mono on true black. No decorative illustrations, no line art characters, no visual metaphors that require decoding.

**When illustration is absolutely necessary** (e.g., a GitHub social card that must communicate the concept in one frame): text-forward composition. The task feed structure as a diagram, not a stylized illustration.

**Never:** isometric device illustrations, outline icons set as hero art, emoji-based empty states, pastel mascots or characters.

---

## Iconography

**Recommended library:** Material Symbols (Google) — outlined variant, weight 200

```
// Android (Compose)
implementation("androidx.compose.material:material-icons-extended")
// or individual symbols via Google Fonts variable font
```

**Rationale:** Material Symbols is the canonical Android icon system. Will runs on Android — using the platform's native icon language is architecturally honest (Sage) and practically correct for users who expect platform conventions. The outlined 200-weight variant reads as lightweight and precise, matching Will's flat, undecorated visual language.

**Alternative:** `phosphor-icons` (thin weight, 1.5px stroke) for non-Android surfaces (GitHub README graphics, open graph).

**Stroke weight:** 1.5–2px at 24dp. Consistent across all uses.

**Size system:**

| Size | Use |
|------|-----|
| 20dp | Inline action icons in task cards (if any) |
| 24dp | Overlay action icons (done, later, delete) — primary interactive surface |
| 32dp | Prominent single-action states |

**Color:** `#F0F0F0` (primary text) for neutral icons. `#4F6EF7` (accent) for active/selected state icon only. `#E05252` for destructive icon. Never multi-color icons.

**Container treatment:** None — bare icons on surface background. No circle, no rounded square container. The border of the container (task card, overlay panel) is the context — icons do not need their own frame.

**Don'ts:**
- No gradient icons
- No 3D or perspective icons
- No squircle illustration-style icons (Shopify/Duolingo register)
- No decorative micro-illustrations in empty states — use text

---

## Textures & Patterns

**Surface treatment:** None. Will's surfaces are flat, true-black, no texture overlay. The terminal preset's CRT scanlines do not apply — Will is a functional tool, not a retro aesthetic.

**CSS:** No texture implementation. No noise SVG, no grain overlay, no halftone.

**Gradient style:** Forbidden. No gradient backgrounds, no gradient cards, no gradient buttons.

**Pattern motifs:** None. The structure of the task feed — ordered task cards with 1dp borders on true black — is the pattern. Information density is the visual motif.

---

## Image Treatments

**Overlay:** No gradient overlays on screenshots. The UI speaks for itself.

**Mask:** No border-radius on screenshots in documentation — flat rectangle crops only. If a screenshot is presented in a card on a README, it sits in a card with `#0D0D0D` background and `1px #2A2A2A` border.

**Blend mode:** None.

**Aspect ratios:**

| Use case | Ratio |
|----------|-------|
| Play Store feature graphic | 16:9 (1024×500px) |
| Play Store screenshot | 9:16 (portrait, 1080×1920px minimum) |
| GitHub README screenshot | Free height, max width 800px |
| Open Graph card | 1200×630px (1.91:1) |
| Android launcher icon | 1:1, 48dp grid (512×512px source) |

**Responsive:** Screenshots are not responsive — they are fixed-dimension assets. Present at 1× and let the HTML/Markdown scale them. No art direction breakpoints.

**Loading:** For any web contexts (GitHub README, landing page): use `loading="lazy"` on screenshot `<img>` tags. No blur-up placeholder needed — dark backgrounds hide the load flash.

---

## Marketing and open source assets

**GitHub README:**
- Header: wordmark only, no hero illustration
- Screenshots inline, dark background, tight crop, no device frame
- No badges beyond essential (license, build status)
- README tone: technical specification, not product landing page

**Play Store listing:**
- Feature graphic: wordmark on true black, one-line descriptor below
- Screenshots: five states — home feed, notification captured, overlay active, app drawer, task done
- No device mockup on screenshots — raw UI on black
- Description in Will's voice: precise, dry, peer-to-peer. No emoji.

**Open Graph / social preview:**
- Dark background `#000000`, wordmark centered or left-aligned
- One-line tagline below ("The workbench. Not the storefront.")
- Optional: task feed screenshot cropped to 800×400, right-aligned in the card

---

## Anti-Patterns

| Don't | Why |
|-------|-----|
| Device mockups | Makes Will look like a lifestyle product |
| Lifestyle photography | Contradicts the tool-not-app positioning |
| Gradient overlays on screenshots | Decoration on functional content |
| Illustration in empty states | Wellness apps use illustration; Will uses text |
| Animated GIFs with multiple states | Each communication is single-state |
| Colorful badges or count indicators | Badge energy is engagement mechanic |
| CRT scanlines or noise texture | Retro cosplay, not the brand's argument |

---

## Related

- [color-system.md](./color-system.md)
