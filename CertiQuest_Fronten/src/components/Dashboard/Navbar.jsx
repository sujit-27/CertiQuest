import { SignedIn, useAuth, UserButton } from '@clerk/clerk-react';
import { Menu, Share2, Wallet, X } from 'lucide-react';
import React, { useEffect, useState } from 'react'
import {Link} from "react-router-dom"
import Sidemenu from './Sidemenu';
import { useDispatch, useSelector } from 'react-redux';
import { fetchUserPoints } from '../../features/pointsSlice';
import PointsDisplay from './PointsDisplay';
import Logo from "../../../public/Logo.png"

const Navbar = ({activeMenu}) => {

  const [openSideMenu, setOpenSideMenu] = useState(false);
  const points = useSelector(state => state.points.points);
  const status = useSelector(state => state.points.status);
  const error = useSelector(state => state.points.error);
  const { getToken, isSignedIn } = useAuth();
  const dispatch = useDispatch();

  useEffect(() => {
    if (isSignedIn) {
      dispatch(fetchUserPoints({ getToken, isSignedIn }));
    }
  }, [dispatch, getToken, isSignedIn]);

  return (
    <>
      <div className='flex items-center justify-between gap-5 bg-white border border-b border-gray-200/50 backdrop-blur-[2px] py-4 px-4 sm:px-7 sticky top-0 z-30'>
        {/* Left Side - menu button and title */}
        <div className='flex items-center gap-5'>
          <button 
            onClick={() => setOpenSideMenu(!openSideMenu)}
            className='block lg:hidden text-black hover:bg-gray-100 p-1 rounded transition-colors'>
              {openSideMenu ? (
                <X className='text-xl'/>
              ) : (
                <Menu className='text-2xl'/>
              )}
          </button>

          <div className='flex items-center gap-2'>
              <img src={Logo} alt="Logo" className="w-10" />
              <span className='text-lg font-semibold text-black truncate'>
                CertiQuest
              </span>
          </div>
        </div>

        {/* Right side - credits and user button */}
        <SignedIn>
          <div className='flex items-center gap-4'>
            <Link to="/subscriptions">
              <PointsDisplay points={points}/>
            </Link>
            <div className='relative'>
                <UserButton/>
            </div>
          </div>
        </SignedIn>

        {/* Mobile side menu */}
        {openSideMenu && (
          <div className='fixed top-[73px] left-0 right-0 bg-white border-b border-gray-200 lg:hidden z-20'>
            {/* Side menu bar  */}
            <Sidemenu activeMenu={activeMenu}/>
          </div>
        )}
      </div>
    </>
  )
}

export default Navbar
