import type { ComponentPropsWithoutRef, ElementType, ReactNode } from 'react'

type Variant = 'fill' | 'outline'
type Size = 'sm' | 'lg'

// as プロパティで <button> / <a> / <Link> のどれにでもなれるボタン
type ButtonProps<T extends ElementType> = {
  as?: T
  variant?: Variant
  size?: Size
  children: ReactNode
} & Omit<ComponentPropsWithoutRef<T>, 'as' | 'children'>

const base =
  'inline-flex items-center justify-center rounded-full border-[1.5px] border-accent font-bold transition cursor-pointer'

const variants: Record<Variant, string> = {
  fill: 'bg-accent text-white hover:opacity-85',
  outline: 'bg-white text-accent hover:bg-accent hover:text-white',
}

const sizes: Record<Size, string> = {
  // ヘッダー用：150×34px（スマホは 84×30px）
  sm: 'h-[30px] min-w-[84px] px-3 text-[11px] tracking-[0.1em] md:h-[34px] md:min-w-[150px] md:text-xs',
  // ログイン送信用：264×70px（スマホは横幅いっぱい・高さ56px）
  lg: 'h-14 w-full text-lg tracking-[0.2em] md:h-[70px] md:w-[264px]',
}

export function Button<T extends ElementType = 'button'>({
  as,
  variant = 'fill',
  size = 'sm',
  className,
  children,
  ...rest
}: ButtonProps<T> & { className?: string }) {
  const Component = as ?? 'button'
  return (
    <Component
      className={`${base} ${variants[variant]} ${sizes[size]} ${className ?? ''}`}
      {...rest}
    >
      {children}
    </Component>
  )
}
