import { CreditCardIcon } from 'lucide-react'
import React from 'react'

const PointsDisplay = ({points}) => {
  return (
    <>
      <div className='flex items-center gap-1 bg-purple-100 py-1 px-3 rounded-full text-purple-800'>
        <CreditCardIcon size={16}/>
        <span className='font-semibold'>{points}</span>
        <span className='text-sm font-semibold'>Points</span>
      </div>
    </>
  )
}

export default PointsDisplay
