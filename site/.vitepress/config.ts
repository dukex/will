import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'Will',
  description: 'an Android launcher that treats your phone as a work surface, not an attention sink.',
  base: '/',
  appearance: 'force-dark',

  head: [
    ['link', { rel: 'preconnect', href: 'https://fonts.googleapis.com' }],
    ['link', { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' }],
    ['link', { href: 'https://fonts.googleapis.com/css2?family=Geist:wght@400;500&family=Geist+Mono:wght@400&display=swap', rel: 'stylesheet' }],
  ],

  themeConfig: {
    nav: [
      { text: 'docs', link: '/docs/' },
      { text: 'github', link: 'https://github.com/dukex/will' },
    ],

    sidebar: {
      '/docs/': [
        {
          text: 'docs',
          items: [
            { text: 'introduction', link: '/docs/' },
            { text: 'getting started', link: '/docs/getting-started' },
            { text: 'architecture', link: '/docs/architecture' },
            { text: 'changelog', link: '/docs/changelog' },
          ],
        },
      ],
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/dukex/will' },
    ],

    footer: {
      message: 'released under the MIT license.',
      copyright: 'made by Emerson Almeida',
    },
  },
})
