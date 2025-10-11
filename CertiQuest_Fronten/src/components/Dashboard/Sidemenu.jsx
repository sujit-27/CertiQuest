import { useUser } from '@clerk/clerk-react'
import { User } from 'lucide-react';
import React from 'react'
import { SIDE_MENU_DATA } from '../../assets/data';
import { useNavigate } from 'react-router-dom';

const Sidemenu = ({activeMenu}) => {

    const {user} = useUser();
    const navigate = useNavigate();

    return (
        <>
        <div className='w-64 h-[calc(100vh - 61px)] bg-white h-screen border-r border-gray-200/50 p-5 sticky top-[61px] z-20'>
            <div className='flex flex-col items-center justify-center gap-3 mt-3 mb-7'>
                {user?.imageUrl ? (
                    <img src={user.imageUrl || ""} alt="Profile image" className='w-20 h-20 bg-slate-400 rounded-full'/>
                ) : (
                    <User className='w-20 h-20 text-xl'/>
                )}

                <h5 className='text-gray-950 font-semibold leading-6'>
                    {user?.fullName || ""}
                </h5>
            </div>

            {SIDE_MENU_DATA.map((item,index) => (
                <button 
                    onClick={() => navigate(item.path)}
                    key={`menu_${index}`}
                    className={` w-full flex items-center gap-4 text-[15px] py-3 px-6 roulg mb-3 transition-all duration-200 cursor-pointer ${activeMenu == item.label ? "bg-purple-800 text-white font-medium rounded-lg shadow-md hover:bg-purple-900" : "hover:bg-gray-100 rounded-xl"}`}
                >
                    <item.icon className='text-xl'/>
                    {item.label}
                </button>
            ))}
        </div>
        </>
    )
}

export default Sidemenu
