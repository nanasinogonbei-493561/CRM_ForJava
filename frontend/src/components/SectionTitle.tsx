import type { ReactNode } from 'react'

type SectionTitleProps = {
  as?: 'h1' | 'h2'
  children: ReactNode
}

// 画面幅いっぱいの横線の上に、白背景のカプセル見出しを重ねる
export function SectionTitle({ as: Tag = 'h2', children }: SectionTitleProps) {
  return (
    <Tag className="relative mt-12 mb-14 text-center before:absolute before:inset-x-0 before:top-1/2 before:border-t before:border-line">
      <span className="relative inline-block rounded-full border-[1.5px] border-line bg-white px-10 py-2 text-lg font-black tracking-[0.15em] md:px-16 md:py-2.5 md:text-[22px]">
        {children}
      </span>
    </Tag>
  )
}