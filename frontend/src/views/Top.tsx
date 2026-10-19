import { SectionTitle } from '../components/SectionTitle'

type Article = {
  id: number
  category: string
  title: string
}

// 仮データ（実際は API から取得する想定）
const ARTICLES: Article[] = [
  { id: 1, category: '?', title: '仮その1' },
  { id: 2, category: '??', title: '仮その2' },
  { id: 3, category: '???', title: '仮その3' },
]

export default function Top() {
  return (
    <main className="mx-auto max-w-site px-5 pb-20">
      <SectionTitle>Data</SectionTitle>

      <div className="grid grid-cols-[repeat(auto-fill,minmax(260px,1fr))] gap-8">
        {ARTICLES.map((article) => (
          <article key={article.id}>
            <a href="#" className="group block">
              <div className="aspect-[4/5] bg-placeholder transition-opacity group-hover:opacity-80" />
              <p className="mt-3 font-en text-xs font-bold tracking-[0.1em] text-accent">
                {article.category}
              </p>
              <h3 className="mt-1.5 text-[15px] leading-relaxed">{article.title}</h3>
            </a>
          </article>
        ))}
      </div>
    </main>
  )
}